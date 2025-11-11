package com.middleware.demo.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka Producer for publishing order events to demonstrate event-driven architecture.
 * This producer publishes events to the 'order-events' topic with proper configuration
 * for reliability and performance.
 */
public class OrderEventProducer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
    private Producer<String, String> producer;
    private static final String TOPIC = "order-events";

    public OrderEventProducer() {
        this(getDefaultProperties());
    }

    public OrderEventProducer(Properties customProps) {
        Properties props = new Properties();
        props.putAll(getDefaultProperties());
        props.putAll(customProps); // Override with custom properties if provided
        
        producer = new KafkaProducer<>(props);
        logger.info("OrderEventProducer initialized with topic: {}", TOPIC);
    }

    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Reliability configurations
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas to acknowledge
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        
        // Performance configurations
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Idempotence for exactly-once semantics
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return props;
    }

    /**
     * Publishes an order event asynchronously with callback handling
     * 
     * @param orderId The unique order identifier (used as message key)
     * @param eventType The type of event (ORDER_CREATED, ORDER_UPDATED, etc.)
     * @param payload The event payload in JSON format
     * @return Future for the record metadata
     */
    public Future<RecordMetadata> publishOrderEvent(String orderId, String eventType, String payload) {
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, orderId, payload);

        // Add event metadata as headers
        record.headers().add("eventType", eventType.getBytes());
        record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
        record.headers().add("source", "order-service".getBytes());

        logger.info("Publishing event - OrderId: {}, EventType: {}", orderId, eventType);

        // Async send with callback
        return producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    logger.info("Event published successfully - Topic: {}, Partition: {}, Offset: {}, OrderId: {}", 
                              metadata.topic(), metadata.partition(), metadata.offset(), orderId);
                } else {
                    logger.error("Failed to publish event for OrderId: {} - Error: {}", orderId, exception.getMessage(), exception);
                }
            }
        });
    }

    /**
     * Publishes an order event synchronously
     * 
     * @param orderId The unique order identifier
     * @param eventType The type of event
     * @param payload The event payload in JSON format
     * @return RecordMetadata containing partition and offset information
     * @throws Exception if the send operation fails
     */
    public RecordMetadata publishOrderEventSync(String orderId, String eventType, String payload) throws Exception {
        Future<RecordMetadata> future = publishOrderEvent(orderId, eventType, payload);
        return future.get(); // Block until completion
    }

    /**
     * Publishes multiple events in batch for better performance
     * 
     * @param events Array of event data
     */
    public void publishBatchEvents(OrderEvent[] events) {
        logger.info("Publishing batch of {} events", events.length);
        
        for (OrderEvent event : events) {
            publishOrderEvent(event.getOrderId(), event.getEventType(), event.getPayload());
        }
        
        // Flush to ensure all messages are sent
        producer.flush();
        logger.info("Batch events flushed successfully");
    }

    /**
     * Gracefully closes the producer and releases resources
     */
    @Override
    public void close() {
        if (producer != null) {
            logger.info("Closing OrderEventProducer...");
            producer.close();
            logger.info("OrderEventProducer closed successfully");
        }
    }

    /**
     * Inner class to represent an order event
     */
    public static class OrderEvent {
        private final String orderId;
        private final String eventType;
        private final String payload;

        public OrderEvent(String orderId, String eventType, String payload) {
            this.orderId = orderId;
            this.eventType = eventType;
            this.payload = payload;
        }

        public String getOrderId() { return orderId; }
        public String getEventType() { return eventType; }
        public String getPayload() { return payload; }
    }
}