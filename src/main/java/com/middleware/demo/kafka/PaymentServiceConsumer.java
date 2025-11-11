package com.middleware.demo.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Payment Service Consumer that processes order events from Kafka.
 * Demonstrates event-driven architecture where payment processing is triggered
 * by order creation events.
 */
public class PaymentServiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceConsumer.class);
    private Consumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TOPIC = "order-events";
    private static final String GROUP_ID = "payment-service-group";
    private static final String PAYMENT_TOPIC = "payment-events";

    public PaymentServiceConsumer() {
        this(getDefaultProperties());
    }

    public PaymentServiceConsumer(Properties customProps) {
        Properties props = new Properties();
        props.putAll(getDefaultProperties());
        props.putAll(customProps); // Override with custom properties if provided
        
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        logger.info("PaymentServiceConsumer initialized for topic: {} with group: {}", TOPIC, GROUP_ID);
    }

    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Consumer behavior configurations
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual commit for better control
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Process in small batches
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        return props;
    }

    /**
     * Starts consuming events from the Kafka topic
     */
    public void startConsuming() {
        running.set(true);
        logger.info("Starting PaymentServiceConsumer...");
        
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                if (!records.isEmpty()) {
                    logger.info("Received {} records for processing", records.count());
                    
                    for (ConsumerRecord<String, String> record : records) {
                        processRecord(record);
                    }
                    
                    // Manual commit after processing all records in the batch
                    try {
                        consumer.commitSync();
                        logger.debug("Successfully committed offsets");
                    } catch (Exception e) {
                        logger.error("Failed to commit offsets", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in consumer loop", e);
        } finally {
            close();
        }
    }

    /**
     * Processes a single Kafka record
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String eventType = extractEventType(record);
            String orderId = record.key();
            String payload = record.value();
            
            logger.info("Processing record - OrderId: {}, EventType: {}, Partition: {}, Offset: {}", 
                       orderId, eventType, record.partition(), record.offset());

            switch (eventType) {
                case "ORDER_CREATED":
                    processOrderCreatedEvent(orderId, payload);
                    break;
                case "ORDER_UPDATED":
                    processOrderUpdatedEvent(orderId, payload);
                    break;
                case "ORDER_CANCELLED":
                    processOrderCancelledEvent(orderId, payload);
                    break;
                default:
                    logger.info("Ignoring event type: {} for OrderId: {}", eventType, orderId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing record for OrderId: {}", record.key(), e);
            // In production, you might want to send to a dead letter queue
        }
    }

    /**
     * Extracts event type from record headers
     */
    private String extractEventType(ConsumerRecord<String, String> record) {
        if (record.headers().lastHeader("eventType") != null) {
            return new String(record.headers().lastHeader("eventType").value());
        }
        return "UNKNOWN";
    }

    /**
     * Processes ORDER_CREATED events
     */
    private void processOrderCreatedEvent(String orderId, String payload) {
        logger.info("Processing payment for new order: {}", orderId);
        
        try {
            // Parse order details
            JsonNode orderData = objectMapper.readTree(payload);
            double amount = orderData.get("amount").asDouble();
            String customerId = orderData.has("customerId") ? orderData.get("customerId").asText() : "unknown";
            
            // Simulate payment processing
            PaymentResult paymentResult = processPayment(orderId, customerId, amount);
            
            if (paymentResult.isSuccess()) {
                logger.info("Payment successful for OrderId: {}, Amount: ${}", orderId, amount);
                publishPaymentConfirmedEvent(orderId, paymentResult);
            } else {
                logger.warn("Payment failed for OrderId: {}, Reason: {}", orderId, paymentResult.getFailureReason());
                publishPaymentFailedEvent(orderId, paymentResult);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment for OrderId: {}", orderId, e);
            publishPaymentFailedEvent(orderId, new PaymentResult(false, "Processing error: " + e.getMessage()));
        }
    }

    /**
     * Processes ORDER_UPDATED events
     */
    private void processOrderUpdatedEvent(String orderId, String payload) {
        logger.info("Processing payment update for order: {}", orderId);
        // Implementation for handling order updates that might affect payment
    }

    /**
     * Processes ORDER_CANCELLED events
     */
    private void processOrderCancelledEvent(String orderId, String payload) {
        logger.info("Processing payment cancellation for order: {}", orderId);
        // Implementation for handling payment refunds or cancellations
    }

    /**
     * Simulates payment processing with payment gateway
     */
    private PaymentResult processPayment(String orderId, String customerId, double amount) {
        logger.info("Processing payment - OrderId: {}, CustomerId: {}, Amount: ${}", orderId, customerId, amount);
        
        // Simulate payment gateway call with some processing time
        try {
            Thread.sleep(100); // Simulate network call
            
            // Simulate payment success/failure (90% success rate)
            boolean success = Math.random() > 0.1;
            
            if (success) {
                String transactionId = "txn_" + System.currentTimeMillis();
                return new PaymentResult(true, transactionId, amount);
            } else {
                return new PaymentResult(false, "Insufficient funds");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PaymentResult(false, "Payment processing interrupted");
        }
    }

    /**
     * Publishes payment confirmed event
     */
    private void publishPaymentConfirmedEvent(String orderId, PaymentResult result) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String paymentPayload = String.format(
                "{\"orderId\":\"%s\",\"status\":\"confirmed\",\"transactionId\":\"%s\",\"amount\":%.2f,\"timestamp\":%d}",
                orderId, result.getTransactionId(), result.getAmount(), System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "PAYMENT_CONFIRMED", paymentPayload);
            logger.info("Published PAYMENT_CONFIRMED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish payment confirmed event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Publishes payment failed event
     */
    private void publishPaymentFailedEvent(String orderId, PaymentResult result) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String paymentPayload = String.format(
                "{\"orderId\":\"%s\",\"status\":\"failed\",\"reason\":\"%s\",\"timestamp\":%d}",
                orderId, result.getFailureReason(), System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "PAYMENT_FAILED", paymentPayload);
            logger.info("Published PAYMENT_FAILED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish payment failed event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Stops the consumer gracefully
     */
    public void stop() {
        logger.info("Stopping PaymentServiceConsumer...");
        running.set(false);
    }

    /**
     * Closes the consumer and releases resources
     */
    public void close() {
        if (consumer != null) {
            logger.info("Closing PaymentServiceConsumer...");
            consumer.close();
            logger.info("PaymentServiceConsumer closed successfully");
        }
    }

    /**
     * Inner class to represent payment processing results
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String failureReason;
        private final double amount;

        public PaymentResult(boolean success, String transactionId, double amount) {
            this.success = success;
            this.transactionId = transactionId;
            this.failureReason = null;
            this.amount = amount;
        }

        public PaymentResult(boolean success, String failureReason) {
            this.success = success;
            this.transactionId = null;
            this.failureReason = failureReason;
            this.amount = 0.0;
        }

        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getFailureReason() { return failureReason; }
        public double getAmount() { return amount; }
    }
}