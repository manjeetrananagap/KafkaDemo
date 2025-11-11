# 🚀 Kafka Demo Quick Start Guide

This guide will help you get the Kafka demo up and running quickly for demonstration purposes.

## 📋 Prerequisites Checklist

- [ ] **Java 11+** installed (`java -version`)
- [ ] **Maven 3.6+** installed (`mvn -version`)
- [ ] **Docker & Docker Compose** installed (`docker --version`)
- [ ] **Eclipse IDE** (optional but recommended)

## 🏃‍♂️ Quick Start (5 minutes)

### Step 1: Start Kafka Infrastructure
```bash
# Navigate to project directory
cd KafkaDemo

# Start Kafka (this will take ~30 seconds)
./scripts/start-kafka.sh simple

# Verify Kafka is running
./scripts/start-kafka.sh status
```

### Step 2: Build the Project
```bash
# Compile the demo
mvn clean compile
```

### Step 3: Run the Demo
```bash
# Run the interactive demo
mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
```

## 🎮 Demo Scenarios

When you run the demo, you'll see an interactive menu:

```
=== Apache Kafka Event-Driven Architecture Demo ===

Choose a demo scenario:
1. 📦 Basic Order Processing Demo
2. 📋 Batch Order Processing Demo  
3. ❌ Order Cancellation Demo
4. 🚀 High Volume Processing Demo
5. 🔄 Event Replay Demo
6. ❌ Exit

Enter your choice (1-6):
```

### Recommended Demo Flow

1. **Start with Scenario 1** - Shows basic event-driven communication
2. **Try Scenario 2** - Demonstrates batch processing and scalability
3. **Explore Scenario 3** - Shows compensation patterns (Saga)
4. **Test Scenario 4** - High-volume stress testing
5. **Finish with Scenario 5** - Event replay and recovery

## 🔍 What You'll See

### Producer Output
```
📤 Publishing order event: ORDER_CREATED for order: ORD-12345
✅ Event published to partition 1 at offset 42
```

### Consumer Output
```
💳 [PaymentService] Processing payment for order: ORD-12345
📦 [InventoryService] Reserving inventory for order: ORD-12345  
📧 [NotificationService] Sending notification for order: ORD-12345
```

## 🛠️ Eclipse IDE Setup

### Import Project
1. **File** → **Import** → **Existing Maven Projects**
2. Browse to `KafkaDemo` directory
3. Click **Finish**

### Run in Eclipse
1. Right-click project → **Run As** → **Java Application**
2. Select `KafkaEventDrivenDemo` 
3. Click **Run**

### Debug Mode
1. Set breakpoints in consumer classes
2. Right-click → **Debug As** → **Java Application**
3. Watch events flow through the system

## 📊 Monitoring

### Kafka Topics
```bash
# List all topics
docker exec kafka-demo-broker-simple kafka-topics --list --bootstrap-server localhost:9092

# View topic details
docker exec kafka-demo-broker-simple kafka-topics --describe --topic order-events --bootstrap-server localhost:9092
```

### Consumer Groups
```bash
# List consumer groups
docker exec kafka-demo-broker-simple kafka-consumer-groups --list --bootstrap-server localhost:9092

# Check consumer lag
docker exec kafka-demo-broker-simple kafka-consumer-groups --describe --group payment-service-group --bootstrap-server localhost:9092
```

## 🎯 Key Learning Points

### Event-Driven Architecture
- **Decoupling**: Services communicate through events, not direct calls
- **Scalability**: Each service can scale independently
- **Resilience**: Failure in one service doesn't affect others

### Kafka Concepts
- **Topics**: Event streams (order-events, payment-events)
- **Partitions**: Parallel processing and ordering guarantees
- **Consumer Groups**: Load balancing and fault tolerance
- **Offsets**: Event replay and recovery capabilities

### Patterns Demonstrated
- **Publish-Subscribe**: One event, multiple subscribers
- **Event Sourcing**: Events as source of truth
- **Saga Pattern**: Distributed transaction handling
- **CQRS**: Command Query Responsibility Segregation

## 🚨 Troubleshooting

### Kafka Won't Start
```bash
# Check Docker
docker ps

# Restart Kafka
./scripts/start-kafka.sh stop
./scripts/start-kafka.sh simple
```

### Connection Errors
```bash
# Wait for Kafka to fully start (30+ seconds)
# Check port accessibility
telnet localhost 9092
```

### Demo Won't Run
```bash
# Ensure project is compiled
mvn clean compile

# Check Java version
java -version  # Should be 11+
```

## 🧹 Cleanup

```bash
# Stop Kafka
./scripts/start-kafka.sh stop

# Remove all data (optional)
./scripts/start-kafka.sh cleanup
```

## 📚 Next Steps

1. **Modify the Code**: Try changing event payloads or adding new consumers
2. **Add New Events**: Create additional event types and handlers
3. **Scale Testing**: Run multiple consumer instances
4. **Production Setup**: Explore Kafka clustering and monitoring

## 💡 Demo Tips

- **Run consumers in separate terminals** to see parallel processing
- **Use Eclipse debugger** to step through event processing
- **Monitor Kafka UI** (if using full setup) at http://localhost:8080
- **Check logs** in `logs/kafka-demo.log` for detailed information

---

**🎯 Happy Kafka Learning!** 

For questions or issues, check the main README.md or the troubleshooting section above.