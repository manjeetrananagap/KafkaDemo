@echo off
REM Script to monitor Kafka topics during demo presentation (Windows)
REM Usage: monitor-demo.bat [kafka-home-directory] [topic-name]

setlocal enabledelayedexpansion

if "%1"=="" (
    set KAFKA_HOME=C:\kafka
) else (
    set KAFKA_HOME=%1
)

if "%2"=="" (
    set TOPIC=order-events
) else (
    set TOPIC=%2
)

set BOOTSTRAP_SERVER=localhost:9092

echo 🔍 Kafka Demo Monitor
echo ====================
echo Kafka Home: %KAFKA_HOME%
echo Topic: %TOPIC%
echo Bootstrap Server: %BOOTSTRAP_SERVER%
echo.

REM Check if Kafka home exists
if not exist "%KAFKA_HOME%" (
    echo ❌ Kafka directory not found: %KAFKA_HOME%
    echo Usage: %0 C:\path\to\kafka [topic-name]
    pause
    exit /b 1
)

REM Check if Kafka is running
netstat -an | findstr :9092 >nul
if errorlevel 1 (
    echo ❌ Kafka is not running on localhost:9092
    pause
    exit /b 1
)

echo 🎯 Monitoring topic: %TOPIC%
echo Press Ctrl+C to stop monitoring
echo ================================
echo.

REM Monitor the topic with enhanced formatting
"%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat" --topic %TOPIC% --bootstrap-server %BOOTSTRAP_SERVER% --from-beginning --property print.headers=true --property print.timestamp=true --property print.partition=true --property print.offset=true --property print.key=true --formatter kafka.tools.DefaultMessageFormatter --property key.separator=" | " --property line.separator="\n---\n"

pause