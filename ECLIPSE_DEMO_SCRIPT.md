# 🎯 Eclipse Demo Presentation Script

This script provides a step-by-step guide for presenting the Kafka demo in Eclipse IDE.

## 🎬 Pre-Demo Setup (5 minutes before presentation)

### 1. Start Local Kafka
```bash
# In your Kafka installation directory
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &
```

### 2. Setup Demo Topics
```bash
cd KafkaDemo
./scripts/setup-local-topics.sh /path/to/your/kafka
```

### 3. Open Eclipse
- Import the KafkaDemo project
- Open key files in tabs:
  - `OrderEventProducer.java`
  - `PaymentServiceConsumer.java`
  - `KafkaEventDrivenDemo.java`

### 4. Prepare Monitoring Terminals
```bash
# Terminal 1: Order events monitor
./scripts/monitor-demo.sh /path/to/kafka order-events

# Terminal 2: Payment events monitor  
./scripts/monitor-demo.sh /path/to/kafka payment-events
```

## 🎤 Demo Presentation Flow (30 minutes)

### Part 1: Architecture Overview (8 minutes)

#### Slide 1: Event-Driven Architecture
> "Today I'll demonstrate event-driven architecture using Apache Kafka. Let's start by understanding what we're building."

**Show Eclipse Project Structure:**
```
📁 com.middleware.demo.kafka
├── 📄 OrderEventProducer.java      ← Event Publisher
├── 📄 PaymentServiceConsumer.java  ← Payment Service
├── 📄 InventoryServiceConsumer.java ← Inventory Service
├── 📄 NotificationServiceConsumer.java ← Notification Service
└── 📄 KafkaEventDrivenDemo.java    ← Demo Runner
```

> "This represents a typical e-commerce order processing system where services communicate through events rather than direct API calls."

#### Slide 2: Key Benefits
> "Event-driven architecture provides several key benefits:"

1. **Decoupling**: Services don't know about each other
2. **Scalability**: Each service scales independently
3. **Resilience**: Failure in one service doesn't affect others
4. **Auditability**: Complete event history for debugging

### Part 2: Code Walkthrough (12 minutes)

#### Demo Point 1: Event Producer (4 minutes)
**Open `OrderEventProducer.java`**

> "Let's examine how we publish events to Kafka."

**Highlight Key Code Sections:**
```java
// Line 25-35: Producer Configuration
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.ACKS_CONFIG, "all"); // Durability guarantee
```

> "Notice we're using 'acks=all' for maximum durability - the producer waits for all replicas to acknowledge."

```java
// Line 45-55: Async Publishing with Callback
producer.send(record, new Callback() {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        // Handle success/failure
    }
});
```

> "Kafka publishing is asynchronous by default. We use callbacks to handle success or failure scenarios."

#### Demo Point 2: Event Consumer (4 minutes)
**Open `PaymentServiceConsumer.java`**

> "Now let's see how services consume and process events."

**Highlight Key Code Sections:**
```java
// Line 30-40: Consumer Configuration
props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
```

> "Consumer groups enable load balancing. Multiple instances of the same service share the workload."

```java
// Line 60-70: Event Processing
String eventType = new String(record.headers().lastHeader("eventType").value());
if ("ORDER_CREATED".equals(eventType)) {
    processOrderCreatedEvent(record.key(), record.value());
}
```

> "We use event headers for routing and metadata. This enables sophisticated event processing patterns."

#### Demo Point 3: Multiple Consumers (4 minutes)
**Show `InventoryServiceConsumer.java` and `NotificationServiceConsumer.java`**

> "The beauty of event-driven architecture is that multiple services can independently react to the same event."

**Highlight Consumer Groups:**
- `payment-service-group`
- `inventory-service-group`  
- `notification-service-group`

> "Each service has its own consumer group, so they all receive the same events independently."

### Part 3: Live Demo (10 minutes)

#### Demo Scenario 1: Basic Order Processing (4 minutes)
**Run the demo in Eclipse:**
1. Right-click `KafkaEventDrivenDemo.java`
2. **Run As** → **Java Application**

**In the console:**
```
Choose demo scenario: 1
Enter order ID: LIVE-DEMO-001
Enter customer email: demo@company.com
Enter order amount: 199.99
```

**Point to monitoring terminals:**
> "Watch the monitoring terminals - you'll see the event flow through the system in real-time."

**Expected Output:**
```
📤 Publishing order event: ORDER_CREATED for order: LIVE-DEMO-001
✅ Event published to partition 1 at offset 42

💳 [PaymentService] Processing payment for order: LIVE-DEMO-001
📦 [InventoryService] Reserving inventory for order: LIVE-DEMO-001
📧 [NotificationService] Sending notification for order: LIVE-DEMO-001
```

> "Notice how all three services processed the same event independently and simultaneously."

#### Demo Scenario 2: Batch Processing (3 minutes)
```
Choose demo scenario: 2
Enter number of orders: 5
```

**Highlight:**
> "Now we're processing multiple orders in batch. Notice the performance metrics and how Kafka handles high throughput."

**Expected Output:**
```
🚀 Starting batch processing of 5 orders...
📊 Batch completed in 245ms
📈 Throughput: 20.4 orders/second
```

#### Demo Scenario 3: Error Handling (3 minutes)
```
Choose demo scenario: 3
Enter order ID: ERROR-DEMO-001
```

**Highlight:**
> "Let's see what happens when payment fails. This demonstrates the Saga pattern for distributed transactions."

**Expected Output:**
```
💳 [PaymentService] Payment failed for order: ERROR-DEMO-001
🔄 [PaymentService] Publishing compensation event: PAYMENT_FAILED
📦 [InventoryService] Releasing reserved inventory for order: ERROR-DEMO-001
```

> "The system automatically handles failures and triggers compensation actions."

### Part 4: Advanced Features & Monitoring (5 minutes)

#### Debug Mode Demonstration (2 minutes)
**Set breakpoints and run in debug mode:**
1. Set breakpoint in `PaymentServiceConsumer.processOrderCreatedEvent()`
2. **Debug As** → **Java Application**
3. Step through the event processing logic

> "Debug mode lets us step through the event processing logic and inspect the event payload and headers."

#### Kafka Monitoring (3 minutes)
**Show monitoring commands:**
```bash
# Consumer group status
bin/kafka-consumer-groups.sh --describe --group payment-service-group --bootstrap-server localhost:9092

# Topic information
bin/kafka-topics.sh --describe --topic order-events --bootstrap-server localhost:9092
```

**Highlight Key Metrics:**
- Consumer lag
- Partition distribution
- Offset management

> "In production, you'd monitor these metrics to ensure healthy event processing."

## 🎯 Key Takeaways (2 minutes)

### Summary Points:
1. **Event-driven architecture enables loose coupling**
2. **Kafka provides reliable, scalable event streaming**
3. **Consumer groups enable horizontal scaling**
4. **Event sourcing provides complete audit trail**
5. **Compensation patterns handle distributed failures**

### Next Steps:
> "This demo shows the fundamentals. In production, you'd add:"
- Schema registry for event evolution
- Monitoring and alerting
- Security and authentication
- Multi-datacenter replication

## 🛠️ Q&A Preparation

### Common Questions:

**Q: How does this compare to REST APIs?**
A: Events are asynchronous and enable loose coupling. REST is synchronous and creates tight coupling.

**Q: What about data consistency?**
A: We use eventual consistency with compensation patterns (Saga) for distributed transactions.

**Q: How do you handle schema evolution?**
A: Kafka Schema Registry provides backward/forward compatibility for event schemas.

**Q: What about performance?**
A: Kafka can handle millions of events per second with proper partitioning and consumer scaling.

**Q: How do you test event-driven systems?**
A: Use embedded Kafka for unit tests, and contract testing for integration tests.

---

**🎯 Demo Complete! Ready for questions.**