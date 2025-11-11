# 🏠 Local Kafka Setup Guide

This guide is for running the demo with your existing local Kafka installation.

## 📋 Prerequisites

- ✅ **Local Kafka** already installed and running
- ✅ **Java 11+** installed
- ✅ **Maven 3.6+** installed
- ✅ **Eclipse IDE** (for demo presentation)

## 🔧 Configuration for Local Kafka

### 1. Verify Kafka is Running

```bash
# Check if Kafka is running (default port 9092)
telnet localhost 9092

# Or check with netstat
netstat -an | grep 9092
```

### 2. Create Demo Topics

```bash
# Navigate to your Kafka installation directory
cd /path/to/your/kafka

# Create order-events topic with 3 partitions
bin/kafka-topics.sh --create \
    --topic order-events \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1

# Create payment-events topic
bin/kafka-topics.sh --create \
    --topic payment-events \
    --bootstrap-server localhost:9092 \
    --partitions 2 \
    --replication-factor 1

# Verify topics were created
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

### 3. Optional: Monitor Topics During Demo

Open separate terminal windows to monitor the topics in real-time:

```bash
# Terminal 1: Monitor order-events
bin/kafka-console-consumer.sh \
    --topic order-events \
    --bootstrap-server localhost:9092 \
    --from-beginning \
    --property print.headers=true \
    --property print.timestamp=true

# Terminal 2: Monitor payment-events  
bin/kafka-console-consumer.sh \
    --topic payment-events \
    --bootstrap-server localhost:9092 \
    --from-beginning \
    --property print.headers=true \
    --property print.timestamp=true
```

## 🚀 Running the Demo

### 1. Build the Project
```bash
cd KafkaDemo
mvn clean compile
```

### 2. Run the Interactive Demo
```bash
mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
```

### 3. Eclipse Demo Setup

#### Import Project
1. **File** → **Import** → **Existing Maven Projects**
2. Browse to `KafkaDemo` directory
3. Click **Finish**

#### Run Configuration for Demo
1. Right-click project → **Run As** → **Run Configurations...**
2. Create new **Java Application** configuration
3. **Main class**: `com.middleware.demo.kafka.KafkaEventDrivenDemo`
4. **Arguments** tab → **VM arguments**: `-Xmx512m -Dlogback.configurationFile=src/main/resources/logback.xml`
5. Click **Apply** and **Run**

#### Debug Configuration for Deep Dive
1. Right-click project → **Debug As** → **Debug Configurations...**
2. Create new **Java Application** configuration
3. Set breakpoints in:
   - `PaymentServiceConsumer.processOrderCreatedEvent()`
   - `InventoryServiceConsumer.processOrderEvent()`
   - `NotificationServiceConsumer.processEvent()`
4. Run in debug mode to step through event processing

## 🎯 Demo Presentation Flow

### 1. Architecture Overview (5 minutes)
- Show the project structure in Eclipse
- Explain event-driven architecture diagram
- Highlight key components: Producer, Consumers, Topics

### 2. Code Walkthrough (10 minutes)
- **OrderEventProducer**: Show async/sync publishing, batching
- **PaymentServiceConsumer**: Demonstrate event processing
- **Consumer Groups**: Explain load balancing and fault tolerance
- **Event Headers**: Show metadata and routing

### 3. Live Demo (15 minutes)

#### Scenario 1: Basic Order Processing
```
Choose demo scenario: 1
Enter order ID: DEMO-001
Enter customer email: demo@example.com
Enter order amount: 299.99
```

**What to highlight:**
- Event published to `order-events` topic
- Multiple consumers process the same event independently
- Asynchronous processing with callbacks

#### Scenario 2: Batch Processing
```
Choose demo scenario: 2
Enter number of orders: 10
```

**What to highlight:**
- High-throughput batch processing
- Partition-based parallel processing
- Performance metrics and throughput

#### Scenario 3: Error Handling & Compensation
```
Choose demo scenario: 3
Enter order ID: DEMO-002
```

**What to highlight:**
- Payment failure simulation
- Compensation events (Saga pattern)
- Event-driven error recovery

### 4. Monitoring & Management (5 minutes)

#### Consumer Groups
```bash
# Show consumer groups
bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9092

# Show consumer lag
bin/kafka-consumer-groups.sh --describe \
    --group payment-service-group \
    --bootstrap-server localhost:9092
```

#### Topic Information
```bash
# Show topic details
bin/kafka-topics.sh --describe \
    --topic order-events \
    --bootstrap-server localhost:9092
```

## 🎨 Demo Customization

### Add Your Own Event Types
```java
// In OrderEventProducer
producer.publishOrderEvent("DEMO-003", "ORDER_SHIPPED", 
    "{\"trackingNumber\":\"TRK123\",\"carrier\":\"DHL\"}");
```

### Create Custom Consumer
```java
// New consumer for shipping events
public class ShippingServiceConsumer {
    // Process ORDER_SHIPPED events
}
```

## 🔍 Troubleshooting

### Kafka Connection Issues
```bash
# Check Kafka process
ps aux | grep kafka

# Check Kafka logs
tail -f /path/to/kafka/logs/server.log
```

### Topic Issues
```bash
# Delete and recreate topics if needed
bin/kafka-topics.sh --delete --topic order-events --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic payment-events --bootstrap-server localhost:9092

# Then recreate them
```

### Demo Application Issues
```bash
# Check Java version
java -version

# Rebuild project
mvn clean compile

# Check application logs
tail -f logs/kafka-demo.log
```

## 📊 Performance Monitoring

### JVM Monitoring
Add these JVM arguments for monitoring:
```
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

### Kafka Metrics
```bash
# Monitor Kafka JMX metrics
bin/kafka-run-class.sh kafka.tools.JmxTool \
    --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec
```

## 🎯 Key Demo Points to Emphasize

1. **Decoupling**: Services don't know about each other
2. **Scalability**: Easy to add new consumers
3. **Reliability**: Event persistence and replay
4. **Performance**: Parallel processing with partitions
5. **Monitoring**: Built-in observability

## 📝 Demo Script Template

```
"Today I'll demonstrate event-driven architecture using Apache Kafka.

[Show Eclipse project structure]
We have a complete e-commerce order processing system with:
- Order service (producer)
- Payment service (consumer)
- Inventory service (consumer)  
- Notification service (consumer)

[Run Scenario 1]
Watch how a single order event triggers multiple independent services...

[Show monitoring terminals]
Notice how events flow through the system in real-time...

[Debug mode]
Let's step through the payment processing logic..."
```

---

**🎯 Ready for your demo presentation!**