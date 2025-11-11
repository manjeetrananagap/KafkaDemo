package com.middleware.demo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main demo class showcasing Apache Kafka Event-Driven Architecture.
 * 
 * This demo demonstrates:
 * 1. Event-driven architecture with multiple microservices
 * 2. Publish-subscribe messaging pattern
 * 3. Event sourcing and stream processing
 * 4. Horizontal scalability with partitions
 * 5. Event persistence and replay capabilities
 * 6. Loose coupling between services
 * 
 * Architecture Overview:
 * - OrderEventProducer: Publishes order events to Kafka
 * - PaymentServiceConsumer: Processes payments for orders
 * - InventoryServiceConsumer: Manages inventory reservations
 * - NotificationServiceConsumer: Sends notifications to customers
 * 
 * Each consumer operates independently with its own consumer group,
 * demonstrating how multiple services can react to the same events.
 */
public class KafkaEventDrivenDemo {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventDrivenDemo.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    // Consumer instances
    private static PaymentServiceConsumer paymentConsumer;
    private static InventoryServiceConsumer inventoryConsumer;
    private static NotificationServiceConsumer notificationConsumer;

    public static void main(String[] args) {
        logger.info("=== Apache Kafka Event-Driven Architecture Demo ===");
        logger.info("This demo showcases event streaming and pub-sub messaging patterns");
        
        // Display demo information
        displayDemoInfo();
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Start consumers
            startConsumers();
            
            // Wait a moment for consumers to initialize
            Thread.sleep(2000);
            
            // Interactive demo menu
            runInteractiveDemo(scanner);
            
        } catch (Exception e) {
            logger.error("Error running demo", e);
        } finally {
            // Cleanup
            stopConsumers();
            executorService.shutdown();
            scanner.close();
            logger.info("Demo completed. Thank you!");
        }
    }

    /**
     * Displays demo information and key learning points
     */
    private static void displayDemoInfo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 KAFKA EVENT-DRIVEN ARCHITECTURE DEMO");
        System.out.println("=".repeat(80));
        System.out.println("📋 What We'll Demonstrate:");
        System.out.println("   • Event producer publishing user activity events");
        System.out.println("   • Multiple consumers subscribing to event topics");
        System.out.println("   • Event partitioning and parallel processing");
        System.out.println("   • Event replay and offset management");
        System.out.println();
        System.out.println("🎯 Key Learning Points:");
        System.out.println("   • Publish-subscribe messaging pattern");
        System.out.println("   • Event sourcing and stream processing");
        System.out.println("   • Horizontal scalability with partitions");
        System.out.println("   • Event persistence and replay capabilities");
        System.out.println();
        System.out.println("🛠️  Technologies Used:");
        System.out.println("   • Apache Kafka broker");
        System.out.println("   • Kafka Java client libraries");
        System.out.println("   • Spring Kafka integration");
        System.out.println("   • Kafka Connect for data integration");
        System.out.println();
        System.out.println("💡 Discover event-driven architecture at scale!");
        System.out.println("=".repeat(80));
        System.out.println();
    }

    /**
     * Starts all consumer services in separate threads
     */
    private static void startConsumers() {
        logger.info("Starting consumer services...");
        
        // Initialize consumers
        paymentConsumer = new PaymentServiceConsumer();
        inventoryConsumer = new InventoryServiceConsumer();
        notificationConsumer = new NotificationServiceConsumer();
        
        // Start consumers in separate threads
        executorService.submit(() -> {
            try {
                paymentConsumer.startConsuming();
            } catch (Exception e) {
                logger.error("Payment consumer error", e);
            }
        });
        
        executorService.submit(() -> {
            try {
                inventoryConsumer.startConsuming();
            } catch (Exception e) {
                logger.error("Inventory consumer error", e);
            }
        });
        
        executorService.submit(() -> {
            try {
                notificationConsumer.startConsuming();
            } catch (Exception e) {
                logger.error("Notification consumer error", e);
            }
        });
        
        logger.info("✓ All consumer services started successfully");
    }

    /**
     * Runs the interactive demo with user input
     */
    private static void runInteractiveDemo(Scanner scanner) throws InterruptedException {
        boolean running = true;
        
        while (running) {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        runBasicOrderDemo();
                        break;
                    case 2:
                        runBatchOrderDemo();
                        break;
                    case 3:
                        runOrderCancellationDemo();
                        break;
                    case 4:
                        runHighVolumeDemo();
                        break;
                    case 5:
                        runEventReplayDemo();
                        break;
                    case 6:
                        displayEventStreamingConcepts();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Displays the interactive menu
     */
    private static void displayMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 KAFKA DEMO MENU");
        System.out.println("=".repeat(60));
        System.out.println("1. 📦 Basic Order Processing Demo");
        System.out.println("2. 📋 Batch Order Processing Demo");
        System.out.println("3. ❌ Order Cancellation Demo");
        System.out.println("4. 🚀 High Volume Processing Demo");
        System.out.println("5. 🔄 Event Replay Demo");
        System.out.println("6. 📚 Event Streaming Concepts");
        System.out.println("0. 🚪 Exit Demo");
        System.out.println("=".repeat(60));
    }

    /**
     * Demonstrates basic order processing flow
     */
    private static void runBasicOrderDemo() throws InterruptedException {
        System.out.println("\n🔥 Running Basic Order Processing Demo...");
        System.out.println("This demonstrates the complete order lifecycle with event-driven architecture.");
        
        try (OrderEventProducer producer = new OrderEventProducer()) {
            // Create a sample order
            String orderId = "ORD-" + System.currentTimeMillis();
            String orderJson = String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"CUST-12345\",\"amount\":299.99,\"productId\":\"PROD-001\",\"quantity\":2}",
                orderId
            );
            
            System.out.println("📤 Publishing ORDER_CREATED event...");
            producer.publishOrderEvent(orderId, "ORDER_CREATED", orderJson);
            
            // Wait for processing
            System.out.println("⏳ Processing order through all services...");
            Thread.sleep(3000);
            
            System.out.println("✅ Order processing completed!");
            System.out.println("   • Payment service processed the payment");
            System.out.println("   • Inventory service reserved the items");
            System.out.println("   • Notification service sent confirmations");
        }
    }

    /**
     * Demonstrates batch order processing
     */
    private static void runBatchOrderDemo() throws InterruptedException {
        System.out.println("\n🔥 Running Batch Order Processing Demo...");
        System.out.println("This demonstrates high-throughput batch processing capabilities.");
        
        try (OrderEventProducer producer = new OrderEventProducer()) {
            // Create multiple orders
            OrderEventProducer.OrderEvent[] orders = new OrderEventProducer.OrderEvent[5];
            
            for (int i = 0; i < 5; i++) {
                String orderId = "BATCH-ORD-" + System.currentTimeMillis() + "-" + i;
                String orderJson = String.format(
                    "{\"orderId\":\"%s\",\"customerId\":\"CUST-%d\",\"amount\":%.2f,\"productId\":\"PROD-%03d\",\"quantity\":%d}",
                    orderId, (12345 + i), (99.99 + i * 50), (i + 1), (i + 1)
                );
                
                orders[i] = new OrderEventProducer.OrderEvent(orderId, "ORDER_CREATED", orderJson);
            }
            
            System.out.println("📤 Publishing batch of 5 orders...");
            producer.publishBatchEvents(orders);
            
            // Wait for processing
            System.out.println("⏳ Processing batch orders...");
            Thread.sleep(5000);
            
            System.out.println("✅ Batch processing completed!");
            System.out.println("   • All orders processed in parallel");
            System.out.println("   • Demonstrates horizontal scalability");
        }
    }

    /**
     * Demonstrates order cancellation flow
     */
    private static void runOrderCancellationDemo() throws InterruptedException {
        System.out.println("\n🔥 Running Order Cancellation Demo...");
        System.out.println("This demonstrates event-driven cancellation and compensation patterns.");
        
        try (OrderEventProducer producer = new OrderEventProducer()) {
            // Create an order first
            String orderId = "CANCEL-ORD-" + System.currentTimeMillis();
            String orderJson = String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"CUST-99999\",\"amount\":199.99,\"productId\":\"PROD-999\",\"quantity\":1}",
                orderId
            );
            
            System.out.println("📤 Publishing ORDER_CREATED event...");
            producer.publishOrderEvent(orderId, "ORDER_CREATED", orderJson);
            
            Thread.sleep(2000);
            
            // Cancel the order
            String cancellationJson = String.format(
                "{\"orderId\":\"%s\",\"reason\":\"Customer requested cancellation\",\"timestamp\":%d}",
                orderId, System.currentTimeMillis()
            );
            
            System.out.println("📤 Publishing ORDER_CANCELLED event...");
            producer.publishOrderEvent(orderId, "ORDER_CANCELLED", cancellationJson);
            
            // Wait for processing
            System.out.println("⏳ Processing cancellation...");
            Thread.sleep(3000);
            
            System.out.println("✅ Order cancellation completed!");
            System.out.println("   • Payment refund initiated");
            System.out.println("   • Inventory reservation released");
            System.out.println("   • Customer notification sent");
        }
    }

    /**
     * Demonstrates high volume processing
     */
    private static void runHighVolumeDemo() throws InterruptedException {
        System.out.println("\n🔥 Running High Volume Processing Demo...");
        System.out.println("This demonstrates Kafka's ability to handle high-throughput scenarios.");
        
        try (OrderEventProducer producer = new OrderEventProducer()) {
            int orderCount = 20;
            System.out.printf("📤 Publishing %d orders rapidly...\n", orderCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < orderCount; i++) {
                String orderId = "VOLUME-ORD-" + System.currentTimeMillis() + "-" + i;
                String orderJson = String.format(
                    "{\"orderId\":\"%s\",\"customerId\":\"CUST-%d\",\"amount\":%.2f,\"productId\":\"PROD-%03d\",\"quantity\":%d}",
                    orderId, (10000 + i), (49.99 + i * 10), (i % 10 + 1), (i % 3 + 1)
                );
                
                producer.publishOrderEvent(orderId, "ORDER_CREATED", orderJson);
                
                // Small delay to avoid overwhelming
                Thread.sleep(50);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.printf("✅ Published %d orders in %d ms\n", orderCount, (endTime - startTime));
            
            // Wait for processing
            System.out.println("⏳ Processing high volume orders...");
            Thread.sleep(8000);
            
            System.out.println("✅ High volume processing completed!");
            System.out.println("   • Demonstrates Kafka's throughput capabilities");
            System.out.println("   • Shows parallel processing across partitions");
        }
    }

    /**
     * Demonstrates event replay capabilities
     */
    private static void runEventReplayDemo() throws InterruptedException {
        System.out.println("\n🔥 Running Event Replay Demo...");
        System.out.println("This demonstrates Kafka's event persistence and replay capabilities.");
        
        System.out.println("📚 Event Replay Concepts:");
        System.out.println("   • Events are persisted in Kafka topics");
        System.out.println("   • Consumers can replay events from any offset");
        System.out.println("   • Useful for debugging, auditing, and recovery");
        System.out.println("   • Enables event sourcing patterns");
        
        try (OrderEventProducer producer = new OrderEventProducer()) {
            // Publish a few events for replay demonstration
            String orderId = "REPLAY-ORD-" + System.currentTimeMillis();
            
            System.out.println("📤 Publishing events for replay demonstration...");
            
            // Order created
            producer.publishOrderEvent(orderId, "ORDER_CREATED", 
                String.format("{\"orderId\":\"%s\",\"amount\":150.00}", orderId));
            Thread.sleep(500);
            
            // Payment confirmed
            producer.publishOrderEvent(orderId, "PAYMENT_CONFIRMED", 
                String.format("{\"orderId\":\"%s\",\"transactionId\":\"txn_123\"}", orderId));
            Thread.sleep(500);
            
            // Inventory reserved
            producer.publishOrderEvent(orderId, "INVENTORY_RESERVED", 
                String.format("{\"orderId\":\"%s\",\"productId\":\"PROD-001\"}", orderId));
            
            System.out.println("✅ Events published and available for replay");
            System.out.println("   • Events are stored in Kafka partitions");
            System.out.println("   • Can be replayed by resetting consumer offsets");
        }
    }

    /**
     * Displays event streaming concepts and architecture
     */
    private static void displayEventStreamingConcepts() {
        System.out.println("\n📚 EVENT STREAMING CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("🏗️  Event-Driven Architecture Benefits:");
        System.out.println("   • Loose Coupling: Services don't directly depend on each other");
        System.out.println("   • Scalability: Each service can scale independently");
        System.out.println("   • Resilience: Failure in one service doesn't affect others");
        System.out.println("   • Flexibility: Easy to add new services or modify existing ones");
        
        System.out.println("\n🔄 Kafka Key Concepts:");
        System.out.println("   • Topics: Categories of events (like order-events)");
        System.out.println("   • Partitions: Parallel processing units within topics");
        System.out.println("   • Consumer Groups: Independent sets of consumers");
        System.out.println("   • Offsets: Position tracking for message consumption");
        
        System.out.println("\n📊 Patterns Demonstrated:");
        System.out.println("   • Publish-Subscribe: Multiple consumers for same events");
        System.out.println("   • Event Sourcing: Events as source of truth");
        System.out.println("   • CQRS: Separate read/write models");
        System.out.println("   • Saga Pattern: Distributed transaction management");
        
        System.out.println("\n🚀 Production Considerations:");
        System.out.println("   • Monitoring: Track consumer lag and throughput");
        System.out.println("   • Error Handling: Dead letter queues for failed messages");
        System.out.println("   • Schema Evolution: Backward/forward compatibility");
        System.out.println("   • Security: Authentication, authorization, encryption");
        
        System.out.println("=".repeat(80));
    }

    /**
     * Stops all consumer services gracefully
     */
    private static void stopConsumers() {
        logger.info("Stopping consumer services...");
        
        if (paymentConsumer != null) {
            paymentConsumer.stop();
        }
        
        if (inventoryConsumer != null) {
            inventoryConsumer.stop();
        }
        
        if (notificationConsumer != null) {
            notificationConsumer.stop();
        }
        
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Timeout waiting for consumers to stop");
            Thread.currentThread().interrupt();
        }
        
        logger.info("✓ All consumer services stopped");
    }
}