@echo off
REM Script to create Kafka topics for the demo on local Kafka installation (Windows)
REM Usage: setup-local-topics.bat [kafka-home-directory]

setlocal enabledelayedexpansion

REM Default Kafka home (adjust as needed)
if "%1"=="" (
    set KAFKA_HOME=C:\kafka
) else (
    set KAFKA_HOME=%1
)

set BOOTSTRAP_SERVER=localhost:9092

echo 🚀 Setting up Kafka topics for demo...
echo Kafka Home: %KAFKA_HOME%
echo Bootstrap Server: %BOOTSTRAP_SERVER%
echo ==================================

REM Check if Kafka home exists
if not exist "%KAFKA_HOME%" (
    echo ❌ Kafka directory not found: %KAFKA_HOME%
    echo Please provide the correct Kafka installation path:
    echo Usage: %0 C:\path\to\kafka
    pause
    exit /b 1
)

REM Check if Kafka is running
echo 🔍 Checking if Kafka is running...
netstat -an | findstr :9092 >nul
if errorlevel 1 (
    echo ❌ Kafka is not running on localhost:9092
    echo Please start Kafka first:
    echo   cd %KAFKA_HOME%
    echo   bin\windows\zookeeper-server-start.bat config\zookeeper.properties
    echo   bin\windows\kafka-server-start.bat config\server.properties
    pause
    exit /b 1
)
echo ✅ Kafka is running

echo.
echo 📋 Creating demo topics...

REM Create order-events topic
echo 📋 Creating topic: order-events (partitions: 3, replication: 1)
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create --topic order-events --bootstrap-server %BOOTSTRAP_SERVER% --partitions 3 --replication-factor 1 --if-not-exists
if %errorlevel% equ 0 (
    echo ✅ Topic 'order-events' created successfully
) else (
    echo ⚠️  Topic 'order-events' might already exist
)

REM Create payment-events topic
echo 📋 Creating topic: payment-events (partitions: 2, replication: 1)
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create --topic payment-events --bootstrap-server %BOOTSTRAP_SERVER% --partitions 2 --replication-factor 1 --if-not-exists
if %errorlevel% equ 0 (
    echo ✅ Topic 'payment-events' created successfully
) else (
    echo ⚠️  Topic 'payment-events' might already exist
)

REM Create inventory-events topic
echo 📋 Creating topic: inventory-events (partitions: 2, replication: 1)
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create --topic inventory-events --bootstrap-server %BOOTSTRAP_SERVER% --partitions 2 --replication-factor 1 --if-not-exists
if %errorlevel% equ 0 (
    echo ✅ Topic 'inventory-events' created successfully
) else (
    echo ⚠️  Topic 'inventory-events' might already exist
)

REM Create notification-events topic
echo 📋 Creating topic: notification-events (partitions: 1, replication: 1)
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create --topic notification-events --bootstrap-server %BOOTSTRAP_SERVER% --partitions 1 --replication-factor 1 --if-not-exists
if %errorlevel% equ 0 (
    echo ✅ Topic 'notification-events' created successfully
) else (
    echo ⚠️  Topic 'notification-events' might already exist
)

echo.
echo 📊 Listing all topics:
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --list --bootstrap-server %BOOTSTRAP_SERVER%

echo.
echo 🔍 Topic details:
echo --- order-events ---
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe --topic order-events --bootstrap-server %BOOTSTRAP_SERVER%
echo.
echo --- payment-events ---
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe --topic payment-events --bootstrap-server %BOOTSTRAP_SERVER%
echo.
echo --- inventory-events ---
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe --topic inventory-events --bootstrap-server %BOOTSTRAP_SERVER%
echo.
echo --- notification-events ---
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe --topic notification-events --bootstrap-server %BOOTSTRAP_SERVER%

echo.
echo ✅ Demo topics setup completed!
echo.
echo 🎯 Ready to run the demo:
echo   cd KafkaDemo
echo   mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
echo.
echo 📊 To monitor topics in real-time, open separate command prompts:
echo   REM Monitor order events
echo   scripts\monitor-demo.bat %KAFKA_HOME% order-events
echo.
echo   REM Monitor payment events
echo   scripts\monitor-demo.bat %KAFKA_HOME% payment-events

pause