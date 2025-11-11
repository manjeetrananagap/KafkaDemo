# Apache Kafka Event-Driven Architecture Demo

🚀 **A comprehensive demonstration of Apache Kafka event streaming and pub-sub messaging patterns**

This project showcases event-driven architecture using Apache Kafka, demonstrating high-throughput event streaming, publish-subscribe messaging, and microservices communication patterns.

## 📋 What We'll Build

- **Event producer** publishing user activity events
- **Multiple consumers** subscribing to event topics  
- **Event partitioning** and parallel processing
- **Event replay** and offset management

## 🎯 Key Learning Points

- **Publish-subscribe messaging pattern**
- **Event sourcing and stream processing** 
- **Horizontal scalability with partitions**
- **Event persistence and replay capabilities**

## 🛠️ Technologies Used

- **Apache Kafka broker**
- **Kafka Java client libraries**
- **Spring Kafka integration**
- **Kafka Connect for data integration**

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Order Service │───▶│  Kafka Broker   │◀───│ Payment Service │
│   (Producer)    │    │  order-events   │    │   (Consumer)    │
└─────────────────┘    │     Topic       │    └─────────────────┘
                       └─────────────────┘
                              │ │ │
                    ┌─────────┘ │ └─────────┐
                    ▼           ▼           ▼
          ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
          │ Inventory Svc   │ │Notification Svc │ │   Other Services│
          │  (Consumer)     │ │   (Consumer)    │ │   (Consumers)   │
          └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- **Java 11+** installed
- **Maven 3.6+** installed  
- **Docker & Docker Compose** installed
- **Eclipse IDE** (recommended)

### 1. Clone and Setup

```bash
git clone <repository-url>
cd KafkaDemo
```

### 2. Start Kafka Infrastructure

```bash
# Start simple Kafka setup (recommended for demo)
./scripts/start-kafka.sh simple

# OR start full setup with UI and additional components
./scripts/start-kafka.sh full
```

### 3. Build the Project

```bash
mvn clean compile
```

### 4. Run the Demo

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"

# OR using Java directly
java -cp target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
     com.middleware.demo.kafka.KafkaEventDrivenDemo
```

## 🎮 Demo Scenarios

The interactive demo provides several scenarios:

### 1. 📦 Basic Order Processing Demo
- Demonstrates complete order lifecycle
- Shows event-driven communication between services
- Illustrates asynchronous processing

### 2. 📋 Batch Order Processing Demo  
- High-throughput batch processing
- Parallel processing across partitions
- Demonstrates scalability

### 3. ❌ Order Cancellation Demo
- Event-driven cancellation flow
- Compensation patterns (Saga pattern)
- Distributed transaction handling

### 4. 🚀 High Volume Processing Demo
- Stress testing with multiple orders
- Performance monitoring
- Throughput demonstration

### 5. 🔄 Event Replay Demo
- Event persistence concepts
- Offset management
- Recovery and debugging scenarios

## 🔧 Eclipse IDE Setup

### Import Project

1. **File** → **Import** → **Existing Maven Projects**
2. Browse to the `KafkaDemo` directory
3. Select the project and click **Finish**

### Run Configuration

1. **Right-click** project → **Run As** → **Java Application**
2. Select `KafkaEventDrivenDemo` as the main class
3. Click **Run**

### Debug Configuration

1. **Right-click** project → **Debug As** → **Java Application**
2. Set breakpoints in consumer classes to observe event processing
3. Use **Variables** view to inspect event payloads

## 📊 Monitoring and Management

### Kafka UI (if using full setup)
- **URL**: http://localhost:8080
- **Features**: Topic management, consumer group monitoring, message browsing

### Command Line Tools

```bash
# List topics
docker exec kafka-demo-broker-simple kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec kafka-demo-broker-simple kafka-topics --describe --topic order-events --bootstrap-server localhost:9092

# List consumer groups
docker exec kafka-demo-broker-simple kafka-consumer-groups --list --bootstrap-server localhost:9092

# Check consumer group status
docker exec kafka-demo-broker-simple kafka-consumer-groups --describe --group payment-service-group --bootstrap-server localhost:9092
```

## 🏛️ Project Structure

```
KafkaDemo/
├── src/main/java/com/middleware/demo/kafka/
│   ├── OrderEventProducer.java           # Event publisher
│   ├── PaymentServiceConsumer.java       # Payment processing service
│   ├── InventoryServiceConsumer.java     # Inventory management service  
│   ├── NotificationServiceConsumer.java  # Notification service
│   └── KafkaEventDrivenDemo.java        # Main demo application
├── docker-compose.yml                    # Full Kafka stack
├── docker-compose-simple.yml            # Simple Kafka setup
├── scripts/
│   └── start-kafka.sh                   # Infrastructure startup script
├── pom.xml                              # Maven dependencies
└── README.md                            # This file
```

## 🔍 Key Components Explained

### OrderEventProducer
- Publishes events to `order-events` topic
- Configures reliability settings (acks=all, retries, idempotence)
- Supports both sync and async publishing
- Includes batch processing capabilities

### PaymentServiceConsumer  
- Consumes from `payment-service-group` consumer group
- Processes ORDER_CREATED events for payment
- Publishes PAYMENT_CONFIRMED/PAYMENT_FAILED events
- Demonstrates manual offset management

### InventoryServiceConsumer
- Consumes from `inventory-service-group` consumer group  
- Manages inventory reservations and commitments
- Handles order cancellations with compensation logic
- Shows event-driven inventory management

### NotificationServiceConsumer
- Consumes from `notification-service-group` consumer group
- Sends notifications for various event types
- Demonstrates fan-out messaging pattern
- Simulates multiple notification channels

## 🎓 Event-Driven Architecture Concepts

### Publish-Subscribe Pattern
- **Decoupling**: Services don't directly depend on each other
- **Scalability**: Each service can scale independently  
- **Resilience**: Failure in one service doesn't affect others
- **Flexibility**: Easy to add new services or modify existing ones

### Event Sourcing
- Events as the source of truth
- Complete audit trail of all changes
- Ability to replay events for debugging or recovery
- Support for temporal queries

### Consumer Groups
- **Load Balancing**: Multiple consumers share partition load
- **Fault Tolerance**: Automatic rebalancing on consumer failure
- **Scalability**: Add consumers to increase processing capacity

### Partitioning Strategy
- **Key-based partitioning**: Orders with same ID go to same partition
- **Parallel processing**: Different partitions processed concurrently
- **Ordering guarantees**: Within-partition message ordering

## 🚨 Troubleshooting

### Common Issues

**Kafka not starting:**
```bash
# Check Docker status
docker ps

# Check logs
docker logs kafka-demo-broker-simple

# Restart infrastructure
./scripts/start-kafka.sh stop
./scripts/start-kafka.sh simple
```

**Connection refused errors:**
```bash
# Ensure Kafka is fully started (wait 30+ seconds)
# Check if port 9092 is accessible
telnet localhost 9092
```

**Consumer lag issues:**
```bash
# Check consumer group status
docker exec kafka-demo-broker-simple kafka-consumer-groups --describe --group payment-service-group --bootstrap-server localhost:9092
```

### Performance Tuning

**Producer Settings:**
- `batch.size`: Increase for higher throughput
- `linger.ms`: Add small delay to batch more messages
- `compression.type`: Use snappy or lz4 for compression

**Consumer Settings:**  
- `max.poll.records`: Adjust batch size for processing
- `fetch.min.bytes`: Control fetch behavior
- `session.timeout.ms`: Adjust for failure detection

## 🧹 Cleanup

```bash
# Stop all services
./scripts/start-kafka.sh stop

# Remove all data and containers
./scripts/start-kafka.sh cleanup
```

## 📚 Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Kafka Tutorials](https://kafka-tutorials.confluent.io/)
- [Event-Driven Architecture Patterns](https://microservices.io/patterns/data/event-sourcing.html)
- [Kafka Best Practices](https://kafka.apache.org/documentation/#bestpractices)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable  
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**🎯 Discover event-driven architecture at scale with Apache Kafka!**