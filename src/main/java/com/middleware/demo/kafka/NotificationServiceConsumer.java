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
 * Notification Service Consumer that processes various events to send notifications.
 * Demonstrates event-driven architecture where notifications are triggered
 * by multiple types of events across different services.
 */
public class NotificationServiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceConsumer.class);
    private Consumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TOPIC = "order-events";
    private static final String GROUP_ID = "notification-service-group";

    public NotificationServiceConsumer() {
        this(getDefaultProperties());
    }

    public NotificationServiceConsumer(Properties customProps) {
        Properties props = new Properties();
        props.putAll(getDefaultProperties());
        props.putAll(customProps);
        
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        logger.info("NotificationServiceConsumer initialized for topic: {} with group: {}", TOPIC, GROUP_ID);
    }

    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 15);
        
        return props;
    }

    /**
     * Starts consuming events from the Kafka topic
     */
    public void startConsuming() {
        running.set(true);
        logger.info("Starting NotificationServiceConsumer...");
        
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                if (!records.isEmpty()) {
                    logger.info("Received {} records for notification processing", records.count());
                    
                    for (ConsumerRecord<String, String> record : records) {
                        processRecord(record);
                    }
                    
                    try {
                        consumer.commitSync();
                        logger.debug("Successfully committed offsets");
                    } catch (Exception e) {
                        logger.error("Failed to commit offsets", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in notification consumer loop", e);
        } finally {
            close();
        }
    }

    /**
     * Processes a single Kafka record for notifications
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String eventType = extractEventType(record);
            String orderId = record.key();
            String payload = record.value();
            
            logger.info("Processing notification record - OrderId: {}, EventType: {}", orderId, eventType);

            switch (eventType) {
                case "ORDER_CREATED":
                    processOrderCreatedNotification(orderId, payload);
                    break;
                case "PAYMENT_CONFIRMED":
                    processPaymentConfirmedNotification(orderId, payload);
                    break;
                case "PAYMENT_FAILED":
                    processPaymentFailedNotification(orderId, payload);
                    break;
                case "INVENTORY_RESERVED":
                    processInventoryReservedNotification(orderId, payload);
                    break;
                case "INVENTORY_RESERVATION_FAILED":
                    processInventoryReservationFailedNotification(orderId, payload);
                    break;
                case "ORDER_CANCELLED":
                    processOrderCancelledNotification(orderId, payload);
                    break;
                default:
                    logger.debug("No notification needed for event type: {}", eventType);
            }
            
        } catch (Exception e) {
            logger.error("Error processing notification record for OrderId: {}", record.key(), e);
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
     * Processes ORDER_CREATED events for customer notifications
     */
    private void processOrderCreatedNotification(String orderId, String payload) {
        try {
            JsonNode orderData = objectMapper.readTree(payload);
            String customerId = orderData.has("customerId") ? orderData.get("customerId").asText() : "unknown";
            double amount = orderData.has("amount") ? orderData.get("amount").asDouble() : 0.0;
            
            NotificationResult result = sendOrderConfirmationNotification(customerId, orderId, amount);
            
            if (result.isSuccess()) {
                logger.info("Order confirmation notification sent successfully for OrderId: {}", orderId);
            } else {
                logger.warn("Failed to send order confirmation notification for OrderId: {}: {}", 
                          orderId, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing order created notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes PAYMENT_CONFIRMED events for payment success notifications
     */
    private void processPaymentConfirmedNotification(String orderId, String payload) {
        try {
            JsonNode paymentData = objectMapper.readTree(payload);
            String transactionId = paymentData.has("transactionId") ? paymentData.get("transactionId").asText() : "N/A";
            double amount = paymentData.has("amount") ? paymentData.get("amount").asDouble() : 0.0;
            
            NotificationResult result = sendPaymentSuccessNotification(orderId, transactionId, amount);
            
            if (result.isSuccess()) {
                logger.info("Payment success notification sent for OrderId: {}", orderId);
            } else {
                logger.warn("Failed to send payment success notification for OrderId: {}: {}", 
                          orderId, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment confirmed notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes PAYMENT_FAILED events for payment failure notifications
     */
    private void processPaymentFailedNotification(String orderId, String payload) {
        try {
            JsonNode paymentData = objectMapper.readTree(payload);
            String reason = paymentData.has("reason") ? paymentData.get("reason").asText() : "Unknown error";
            
            NotificationResult result = sendPaymentFailureNotification(orderId, reason);
            
            if (result.isSuccess()) {
                logger.info("Payment failure notification sent for OrderId: {}", orderId);
            } else {
                logger.warn("Failed to send payment failure notification for OrderId: {}: {}", 
                          orderId, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment failed notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes INVENTORY_RESERVED events for inventory confirmation notifications
     */
    private void processInventoryReservedNotification(String orderId, String payload) {
        try {
            JsonNode inventoryData = objectMapper.readTree(payload);
            String productId = inventoryData.has("productId") ? inventoryData.get("productId").asText() : "N/A";
            int quantity = inventoryData.has("quantity") ? inventoryData.get("quantity").asInt() : 0;
            
            NotificationResult result = sendInventoryReservedNotification(orderId, productId, quantity);
            
            if (result.isSuccess()) {
                logger.info("Inventory reserved notification sent for OrderId: {}", orderId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing inventory reserved notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes INVENTORY_RESERVATION_FAILED events for inventory failure notifications
     */
    private void processInventoryReservationFailedNotification(String orderId, String payload) {
        try {
            JsonNode inventoryData = objectMapper.readTree(payload);
            String reason = inventoryData.has("reason") ? inventoryData.get("reason").asText() : "Unknown error";
            
            NotificationResult result = sendInventoryFailureNotification(orderId, reason);
            
            if (result.isSuccess()) {
                logger.info("Inventory failure notification sent for OrderId: {}", orderId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing inventory failure notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes ORDER_CANCELLED events for cancellation notifications
     */
    private void processOrderCancelledNotification(String orderId, String payload) {
        try {
            NotificationResult result = sendOrderCancellationNotification(orderId);
            
            if (result.isSuccess()) {
                logger.info("Order cancellation notification sent for OrderId: {}", orderId);
            } else {
                logger.warn("Failed to send order cancellation notification for OrderId: {}: {}", 
                          orderId, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing order cancelled notification for OrderId: {}", orderId, e);
        }
    }

    /**
     * Simulates sending order confirmation notification
     */
    private NotificationResult sendOrderConfirmationNotification(String customerId, String orderId, double amount) {
        logger.info("Sending order confirmation notification - CustomerId: {}, OrderId: {}, Amount: ${}", 
                   customerId, orderId, amount);
        
        try {
            // Simulate notification service call
            Thread.sleep(30);
            
            String message = String.format("Your order %s for $%.2f has been received and is being processed.", 
                                         orderId, amount);
            
            return simulateNotificationDelivery("ORDER_CONFIRMATION", customerId, message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates sending payment success notification
     */
    private NotificationResult sendPaymentSuccessNotification(String orderId, String transactionId, double amount) {
        logger.info("Sending payment success notification - OrderId: {}, TransactionId: {}, Amount: ${}", 
                   orderId, transactionId, amount);
        
        try {
            Thread.sleep(30);
            
            String message = String.format("Payment of $%.2f for order %s has been processed successfully. Transaction ID: %s", 
                                         amount, orderId, transactionId);
            
            return simulateNotificationDelivery("PAYMENT_SUCCESS", "customer", message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates sending payment failure notification
     */
    private NotificationResult sendPaymentFailureNotification(String orderId, String reason) {
        logger.info("Sending payment failure notification - OrderId: {}, Reason: {}", orderId, reason);
        
        try {
            Thread.sleep(30);
            
            String message = String.format("Payment for order %s failed: %s. Please try again or use a different payment method.", 
                                         orderId, reason);
            
            return simulateNotificationDelivery("PAYMENT_FAILURE", "customer", message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates sending inventory reserved notification
     */
    private NotificationResult sendInventoryReservedNotification(String orderId, String productId, int quantity) {
        logger.info("Sending inventory reserved notification - OrderId: {}, ProductId: {}, Quantity: {}", 
                   orderId, productId, quantity);
        
        try {
            Thread.sleep(20);
            
            String message = String.format("Inventory for %d units of product %s has been reserved for order %s.", 
                                         quantity, productId, orderId);
            
            return simulateNotificationDelivery("INVENTORY_RESERVED", "internal", message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates sending inventory failure notification
     */
    private NotificationResult sendInventoryFailureNotification(String orderId, String reason) {
        logger.info("Sending inventory failure notification - OrderId: {}, Reason: {}", orderId, reason);
        
        try {
            Thread.sleep(30);
            
            String message = String.format("Unable to reserve inventory for order %s: %s. Order may be cancelled.", 
                                         orderId, reason);
            
            return simulateNotificationDelivery("INVENTORY_FAILURE", "customer", message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates sending order cancellation notification
     */
    private NotificationResult sendOrderCancellationNotification(String orderId) {
        logger.info("Sending order cancellation notification - OrderId: {}", orderId);
        
        try {
            Thread.sleep(30);
            
            String message = String.format("Your order %s has been cancelled. Any charges will be refunded within 3-5 business days.", 
                                         orderId);
            
            return simulateNotificationDelivery("ORDER_CANCELLATION", "customer", message);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new NotificationResult(false, "Notification sending interrupted");
        }
    }

    /**
     * Simulates notification delivery (email, SMS, push notification, etc.)
     */
    private NotificationResult simulateNotificationDelivery(String notificationType, String recipient, String message) {
        // Simulate notification delivery success rate (98% success)
        boolean success = Math.random() > 0.02;
        
        if (success) {
            logger.info("✓ {} notification delivered to {}: {}", notificationType, recipient, message);
            return new NotificationResult(true, null);
        } else {
            String error = "Notification delivery failed - service unavailable";
            logger.warn("✗ {} notification failed for {}: {}", notificationType, recipient, error);
            return new NotificationResult(false, error);
        }
    }

    /**
     * Stops the consumer gracefully
     */
    public void stop() {
        logger.info("Stopping NotificationServiceConsumer...");
        running.set(false);
    }

    /**
     * Closes the consumer and releases resources
     */
    public void close() {
        if (consumer != null) {
            logger.info("Closing NotificationServiceConsumer...");
            consumer.close();
            logger.info("NotificationServiceConsumer closed successfully");
        }
    }

    /**
     * Inner class to represent notification delivery results
     */
    public static class NotificationResult {
        private final boolean success;
        private final String errorMessage;

        public NotificationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
}