# 🪟 Windows Kafka Demo Setup Guide

This guide is specifically for running the Kafka demo on Windows with your local Kafka installation.

## 📋 Prerequisites

- ✅ **Local Kafka** installed on Windows
- ✅ **Java 11+** installed (`java -version`)
- ✅ **Maven 3.6+** installed (`mvn -version`)
- ✅ **Eclipse IDE** (for demo presentation)

## 🔧 Windows Kafka Setup

### 1. Verify Kafka Installation

```cmd
# Check if Kafka is installed (typical locations)
dir C:\kafka
# OR
dir C:\apache-kafka*
# OR check your custom installation path
```

### 2. Start Kafka Services

Open **two separate Command Prompt windows as Administrator**:

**Command Prompt 1 - Start Zookeeper:**
```cmd
cd C:\kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

**Command Prompt 2 - Start Kafka Broker:**
```cmd
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

### 3. Verify Kafka is Running

```cmd
# Check if Kafka is listening on port 9092
netstat -an | findstr :9092
```

## 🚀 Quick Demo Setup

### Option 1: Automated Setup (Recommended)
```cmd
cd KafkaDemo
scripts\start-demo.bat C:\kafka
```

### Option 2: Manual Setup
```cmd
# 1. Create demo topics
scripts\setup-local-topics.bat C:\kafka

# 2. Build the project
mvn clean compile

# 3. Run the demo
mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
```

## 🔍 Monitoring During Demo

### Real-time Event Monitoring
Open **separate Command Prompt windows** for monitoring:

**Command Prompt 3 - Monitor Order Events:**
```cmd
cd KafkaDemo
scripts\monitor-demo.bat C:\kafka order-events
```

**Command Prompt 4 - Monitor Payment Events:**
```cmd
cd KafkaDemo
scripts\monitor-demo.bat C:\kafka payment-events
```

### Manual Kafka Commands
```cmd
# List all topics
C:\kafka\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Describe a topic
C:\kafka\bin\windows\kafka-topics.bat --describe --topic order-events --bootstrap-server localhost:9092

# List consumer groups
C:\kafka\bin\windows\kafka-consumer-groups.bat --list --bootstrap-server localhost:9092

# Check consumer lag
C:\kafka\bin\windows\kafka-consumer-groups.bat --describe --group payment-service-group --bootstrap-server localhost:9092
```

## 🎯 Eclipse IDE Setup on Windows

### 1. Import Project
1. Open Eclipse IDE
2. **File** → **Import** → **Existing Maven Projects**
3. Browse to your `KafkaDemo` directory
4. Click **Finish**

### 2. Configure Java Build Path
1. Right-click project → **Properties**
2. **Java Build Path** → **Libraries**
3. Ensure **Modulepath** or **Classpath** has:
   - JRE System Library (Java 11+)
   - Maven Dependencies

### 3. Run Configuration
1. Right-click `KafkaEventDrivenDemo.java`
2. **Run As** → **Run Configurations...**
3. Create new **Java Application**
4. **Main class**: `com.middleware.demo.kafka.KafkaEventDrivenDemo`
5. **Arguments** tab → **VM arguments**: 
   ```
   -Xmx512m -Dlogback.configurationFile=src/main/resources/logback.xml
   ```
6. Click **Apply** and **Run**

### 4. Debug Configuration
1. Set breakpoints in consumer classes:
   - `PaymentServiceConsumer.processOrderCreatedEvent()`
   - `InventoryServiceConsumer.processOrderEvent()`
2. Right-click → **Debug As** → **Java Application**

## 🎬 Windows Demo Presentation Flow

### Pre-Demo Setup (5 minutes)
1. **Start Kafka services** (Zookeeper + Broker)
2. **Run setup script**: `scripts\start-demo.bat C:\kafka`
3. **Open Eclipse** and import project
4. **Prepare monitoring windows** (4 Command Prompts total)

### Demo Window Layout
```
┌─────────────────┬─────────────────┐
│   Eclipse IDE   │  Order Events   │
│   (Main Demo)   │   (Monitor)     │
├─────────────────┼─────────────────┤
│ Payment Events  │   Demo Output   │
│   (Monitor)     │   (Console)     │
└─────────────────┴─────────────────┘
```

### Demo Scenarios

#### 1. Basic Order Processing
**In Eclipse Console:**
```
Choose demo scenario: 1
Enter order ID: WIN-DEMO-001
Enter customer email: demo@company.com
Enter order amount: 299.99
```

**Expected Output:**
```
📤 Publishing order event: ORDER_CREATED for order: WIN-DEMO-001
✅ Event published to partition 1 at offset 42
💳 [PaymentService] Processing payment for order: WIN-DEMO-001
📦 [InventoryService] Reserving inventory for order: WIN-DEMO-001
📧 [NotificationService] Sending notification for order: WIN-DEMO-001
```

#### 2. Batch Processing Demo
```
Choose demo scenario: 2
Enter number of orders: 10
```

#### 3. Error Handling Demo
```
Choose demo scenario: 3
Enter order ID: ERROR-WIN-001
```

## 🛠️ Windows-Specific Troubleshooting

### Common Windows Issues

#### Kafka Won't Start
```cmd
# Check if ports are in use
netstat -an | findstr :2181
netstat -an | findstr :9092

# Kill processes if needed
taskkill /F /IM java.exe
```

#### Path Issues
```cmd
# Ensure JAVA_HOME is set
echo %JAVA_HOME%

# Add to PATH if needed
set PATH=%PATH%;C:\kafka\bin\windows
```

#### Permission Issues
- Run Command Prompt as **Administrator**
- Check Windows Defender/Antivirus settings
- Ensure Kafka directory has write permissions

#### Maven Issues
```cmd
# Check Maven installation
mvn -version

# Clear Maven cache if needed
rmdir /s %USERPROFILE%\.m2\repository
```

### Windows Firewall
If you have issues with connections:
1. Open **Windows Defender Firewall**
2. **Allow an app through firewall**
3. Add **Java** and **Eclipse** if not present

## 📊 Performance Optimization for Windows

### JVM Settings for Demo
```cmd
# For better performance on Windows
set JAVA_OPTS=-Xmx1024m -XX:+UseG1GC -Djava.awt.headless=true
mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
```

### Kafka Configuration Tuning
Edit `C:\kafka\config\server.properties`:
```properties
# Increase buffer sizes for Windows
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# Optimize for Windows file system
log.flush.interval.messages=10000
log.flush.interval.ms=1000
```

## 🎯 Demo Checklist for Windows

### Before Demo
- [ ] Zookeeper started
- [ ] Kafka broker started  
- [ ] Topics created (`scripts\setup-local-topics.bat`)
- [ ] Project built (`mvn clean compile`)
- [ ] Eclipse project imported
- [ ] Monitoring windows prepared

### During Demo
- [ ] Show Windows-specific Kafka commands
- [ ] Demonstrate Eclipse integration
- [ ] Use Windows Command Prompt for monitoring
- [ ] Highlight Windows file paths and commands

### After Demo
- [ ] Stop Kafka services gracefully
- [ ] Close monitoring windows
- [ ] Save Eclipse workspace

## 📝 Windows Command Reference

### Essential Commands
```cmd
# Start services
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
bin\windows\kafka-server-start.bat config\server.properties

# Topic management
bin\windows\kafka-topics.bat --create --topic test --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Consumer/Producer testing
bin\windows\kafka-console-producer.bat --topic test --bootstrap-server localhost:9092
bin\windows\kafka-console-consumer.bat --topic test --bootstrap-server localhost:9092 --from-beginning

# Consumer groups
bin\windows\kafka-consumer-groups.bat --list --bootstrap-server localhost:9092
```

## 🎯 Success Tips for Windows Demo

1. **Use multiple Command Prompt windows** - easier to manage than tabs
2. **Run as Administrator** - avoids permission issues
3. **Check Windows Defender** - may block Kafka ports
4. **Use full paths** - avoid PATH issues during demo
5. **Prepare window layout** - arrange windows for easy switching
6. **Test beforehand** - Windows can have unique networking issues

---

**🚀 Your Windows Kafka demo is ready!**

**Next Steps:**
1. Test the complete setup with `scripts\start-demo.bat`
2. Practice the demo scenarios in Eclipse
3. Prepare for Windows-specific questions

**Good luck with your Windows demo! 🎯**