# 🪟 Windows Kafka Demo - Quick Start Guide

**Perfect for Windows laptops with local Kafka installation!**

## ⚡ 5-Minute Setup

### 1. Prerequisites Check
```cmd
REM Check Java (need 11+)
java -version

REM Check Maven
mvn -version

REM Check if Kafka is installed
dir C:\kafka
```

### 2. Start Kafka Services
**Open 2 separate Command Prompts as Administrator:**

**Command Prompt 1:**
```cmd
cd C:\kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

**Command Prompt 2:**
```cmd
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

### 3. Setup Demo (Automated)
**Command Prompt 3:**
```cmd
cd KafkaDemo
scripts\start-demo.bat C:\kafka
```

### 4. Import into Eclipse
1. **File** → **Import** → **Existing Maven Projects**
2. Browse to `KafkaDemo` folder
3. **Finish**

### 5. Run Demo
- Right-click `KafkaEventDrivenDemo.java`
- **Run As** → **Java Application**

## 🔍 Monitor Events (Optional)

**Command Prompt 4:**
```cmd
cd KafkaDemo
scripts\monitor-demo.bat C:\kafka order-events
```

**Command Prompt 5:**
```cmd
cd KafkaDemo
scripts\monitor-demo.bat C:\kafka payment-events
```

## 🎯 Demo Scenarios

1. **Basic Order Processing** - Choose option 1
2. **Batch Processing** - Choose option 2  
3. **Error Handling** - Choose option 3
4. **High Volume** - Choose option 4
5. **Event Replay** - Choose option 5

## 🛠️ Troubleshooting

### Kafka Won't Start
```cmd
REM Check if ports are free
netstat -an | findstr :9092
netstat -an | findstr :2181

REM Kill Java processes if needed
taskkill /F /IM java.exe
```

### Build Issues
```cmd
REM Clean and rebuild
mvn clean compile

REM Check JAVA_HOME
echo %JAVA_HOME%
```

### Eclipse Issues
- Ensure Java 11+ is configured
- Check Maven integration is installed
- Refresh project (F5)

## 📊 Window Layout for Demo

```
┌─────────────────┬─────────────────┐
│   Eclipse IDE   │  Order Monitor  │
│   (Main Demo)   │  (Command Prompt│
├─────────────────┼─────────────────┤
│ Payment Monitor │   Kafka Logs    │
│ (Command Prompt)│ (Command Prompt)│
└─────────────────┴─────────────────┘
```

## 🎬 Presentation Flow

1. **Show Architecture** (5 min)
   - Explain event-driven concepts
   - Show project structure in Eclipse

2. **Basic Demo** (10 min)
   - Run scenario 1 (Basic Order)
   - Show real-time monitoring
   - Explain async processing

3. **Advanced Features** (10 min)
   - Batch processing (scenario 2)
   - Error handling (scenario 3)
   - Show consumer groups

4. **Q&A** (5 min)
   - Use monitoring windows
   - Show Kafka commands

## 🚀 Success!

Your Windows Kafka demo is ready! 

**Key Files:**
- `WINDOWS_SETUP.md` - Detailed setup
- `scripts\*.bat` - Windows automation
- `src\main\java\...` - Demo code

**Good luck with your presentation! 🎯**