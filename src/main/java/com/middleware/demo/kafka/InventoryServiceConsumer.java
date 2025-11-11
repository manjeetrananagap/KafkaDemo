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
 * Inventory Service Consumer that processes order events to manage inventory.
 * Demonstrates event-driven architecture where inventory management is triggered
 * by order events independently from payment processing.
 */
public class InventoryServiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceConsumer.class);
    private Consumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TOPIC = "order-events";
    private static final String GROUP_ID = "inventory-service-group";

    public InventoryServiceConsumer() {
        this(getDefaultProperties());
    }

    public InventoryServiceConsumer(Properties customProps) {
        Properties props = new Properties();
        props.putAll(getDefaultProperties());
        props.putAll(customProps);
        
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        logger.info("InventoryServiceConsumer initialized for topic: {} with group: {}", TOPIC, GROUP_ID);
    }

    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);
        
        return props;
    }

    /**
     * Starts consuming events from the Kafka topic
     */
    public void startConsuming() {
        running.set(true);
        logger.info("Starting InventoryServiceConsumer...");
        
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                if (!records.isEmpty()) {
                    logger.info("Received {} records for inventory processing", records.count());
                    
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
            logger.error("Error in inventory consumer loop", e);
        } finally {
            close();
        }
    }

    /**
     * Processes a single Kafka record for inventory management
     */
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String eventType = extractEventType(record);
            String orderId = record.key();
            String payload = record.value();
            
            logger.info("Processing inventory record - OrderId: {}, EventType: {}", orderId, eventType);

            switch (eventType) {
                case "ORDER_CREATED":
                    processOrderCreatedEvent(orderId, payload);
                    break;
                case "PAYMENT_CONFIRMED":
                    processPaymentConfirmedEvent(orderId, payload);
                    break;
                case "ORDER_CANCELLED":
                    processOrderCancelledEvent(orderId, payload);
                    break;
                default:
                    logger.debug("Ignoring event type: {} for inventory processing", eventType);
            }
            
        } catch (Exception e) {
            logger.error("Error processing inventory record for OrderId: {}", record.key(), e);
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
     * Processes ORDER_CREATED events for inventory reservation
     */
    private void processOrderCreatedEvent(String orderId, String payload) {
        logger.info("Reserving inventory for order: {}", orderId);
        
        try {
            JsonNode orderData = objectMapper.readTree(payload);
            
            // Extract product information
            if (orderData.has("items")) {
                JsonNode items = orderData.get("items");
                for (JsonNode item : items) {
                    String productId = item.get("productId").asText();
                    int quantity = item.get("quantity").asInt();
                    
                    InventoryResult result = reserveInventory(productId, quantity, orderId);
                    
                    if (result.isSuccess()) {
                        logger.info("Successfully reserved {} units of product {} for order {}", 
                                  quantity, productId, orderId);
                    } else {
                        logger.warn("Failed to reserve inventory for product {} in order {}: {}", 
                                  productId, orderId, result.getMessage());
                        publishInventoryReservationFailedEvent(orderId, productId, result.getMessage());
                    }
                }
            } else {
                // Handle simple order format
                String productId = orderData.has("productId") ? orderData.get("productId").asText() : "default-product";
                int quantity = orderData.has("quantity") ? orderData.get("quantity").asInt() : 1;
                
                InventoryResult result = reserveInventory(productId, quantity, orderId);
                if (result.isSuccess()) {
                    logger.info("Successfully reserved {} units of product {} for order {}", 
                              quantity, productId, orderId);
                    publishInventoryReservedEvent(orderId, productId, quantity);
                } else {
                    logger.warn("Failed to reserve inventory for order {}: {}", orderId, result.getMessage());
                    publishInventoryReservationFailedEvent(orderId, productId, result.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing inventory reservation for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes PAYMENT_CONFIRMED events to commit inventory reservation
     */
    private void processPaymentConfirmedEvent(String orderId, String payload) {
        logger.info("Committing inventory reservation for paid order: {}", orderId);
        
        try {
            // In a real system, this would commit the reserved inventory
            // and update stock levels permanently
            boolean success = commitInventoryReservation(orderId);
            
            if (success) {
                logger.info("Successfully committed inventory for order: {}", orderId);
                publishInventoryCommittedEvent(orderId);
            } else {
                logger.error("Failed to commit inventory for order: {}", orderId);
            }
            
        } catch (Exception e) {
            logger.error("Error committing inventory for OrderId: {}", orderId, e);
        }
    }

    /**
     * Processes ORDER_CANCELLED events to release inventory reservation
     */
    private void processOrderCancelledEvent(String orderId, String payload) {
        logger.info("Releasing inventory reservation for cancelled order: {}", orderId);
        
        try {
            boolean success = releaseInventoryReservation(orderId);
            
            if (success) {
                logger.info("Successfully released inventory for cancelled order: {}", orderId);
                publishInventoryReleasedEvent(orderId);
            } else {
                logger.error("Failed to release inventory for cancelled order: {}", orderId);
            }
            
        } catch (Exception e) {
            logger.error("Error releasing inventory for OrderId: {}", orderId, e);
        }
    }

    /**
     * Simulates inventory reservation
     */
    private InventoryResult reserveInventory(String productId, int quantity, String orderId) {
        logger.info("Reserving inventory - ProductId: {}, Quantity: {}, OrderId: {}", 
                   productId, quantity, orderId);
        
        try {
            Thread.sleep(50); // Simulate database call
            
            // Simulate inventory check (95% success rate)
            boolean available = Math.random() > 0.05;
            
            if (available) {
                return new InventoryResult(true, "Inventory reserved successfully");
            } else {
                return new InventoryResult(false, "Insufficient inventory");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new InventoryResult(false, "Inventory reservation interrupted");
        }
    }

    /**
     * Simulates committing inventory reservation
     */
    private boolean commitInventoryReservation(String orderId) {
        logger.info("Committing inventory reservation for OrderId: {}", orderId);
        // Simulate database update
        return true;
    }

    /**
     * Simulates releasing inventory reservation
     */
    private boolean releaseInventoryReservation(String orderId) {
        logger.info("Releasing inventory reservation for OrderId: {}", orderId);
        // Simulate database update
        return true;
    }

    /**
     * Publishes inventory reserved event
     */
    private void publishInventoryReservedEvent(String orderId, String productId, int quantity) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String inventoryPayload = String.format(
                "{\"orderId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"status\":\"reserved\",\"timestamp\":%d}",
                orderId, productId, quantity, System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "INVENTORY_RESERVED", inventoryPayload);
            logger.info("Published INVENTORY_RESERVED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish inventory reserved event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Publishes inventory reservation failed event
     */
    private void publishInventoryReservationFailedEvent(String orderId, String productId, String reason) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String inventoryPayload = String.format(
                "{\"orderId\":\"%s\",\"productId\":\"%s\",\"status\":\"reservation_failed\",\"reason\":\"%s\",\"timestamp\":%d}",
                orderId, productId, reason, System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "INVENTORY_RESERVATION_FAILED", inventoryPayload);
            logger.info("Published INVENTORY_RESERVATION_FAILED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish inventory reservation failed event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Publishes inventory committed event
     */
    private void publishInventoryCommittedEvent(String orderId) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String inventoryPayload = String.format(
                "{\"orderId\":\"%s\",\"status\":\"committed\",\"timestamp\":%d}",
                orderId, System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "INVENTORY_COMMITTED", inventoryPayload);
            logger.info("Published INVENTORY_COMMITTED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish inventory committed event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Publishes inventory released event
     */
    private void publishInventoryReleasedEvent(String orderId) {
        try (OrderEventProducer producer = new OrderEventProducer()) {
            String inventoryPayload = String.format(
                "{\"orderId\":\"%s\",\"status\":\"released\",\"timestamp\":%d}",
                orderId, System.currentTimeMillis()
            );
            
            producer.publishOrderEvent(orderId, "INVENTORY_RELEASED", inventoryPayload);
            logger.info("Published INVENTORY_RELEASED event for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Failed to publish inventory released event for OrderId: {}", orderId, e);
        }
    }

    /**
     * Stops the consumer gracefully
     */
    public void stop() {
        logger.info("Stopping InventoryServiceConsumer...");
        running.set(false);
    }

    /**
     * Closes the consumer and releases resources
     */
    public void close() {
        if (consumer != null) {
            logger.info("Closing InventoryServiceConsumer...");
            consumer.close();
            logger.info("InventoryServiceConsumer closed successfully");
        }
    }

    /**
     * Inner class to represent inventory operation results
     */
    public static class InventoryResult {
        private final boolean success;
        private final String message;

        public InventoryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}