@echo off
REM Quick start script for Kafka demo on Windows
REM Usage: start-demo.bat [kafka-home-directory]

setlocal enabledelayedexpansion

if "%1"=="" (
    set KAFKA_HOME=C:\kafka
) else (
    set KAFKA_HOME=%1
)

echo 🚀 Kafka Demo Quick Start (Windows)
echo ==================================
echo Kafka Home: %KAFKA_HOME%
echo.

REM Check if Maven is available
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven not found. Please install Maven first.
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found. Please install Java 11+ first.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed
echo.

REM Setup topics
echo 📋 Setting up Kafka topics...
call scripts\setup-local-topics.bat %KAFKA_HOME%
if errorlevel 1 (
    echo ❌ Failed to setup topics
    pause
    exit /b 1
)

echo.
echo 🔨 Building the project...
mvn clean compile
if errorlevel 1 (
    echo ❌ Build failed
    pause
    exit /b 1
)

echo.
echo ✅ Setup complete! 
echo.
echo 🎯 To run the demo:
echo   mvn exec:java -Dexec.mainClass="com.middleware.demo.kafka.KafkaEventDrivenDemo"
echo.
echo 📊 To monitor events, open new command prompts and run:
echo   scripts\monitor-demo.bat %KAFKA_HOME% order-events
echo   scripts\monitor-demo.bat %KAFKA_HOME% payment-events
echo.
echo 🎬 For Eclipse setup, see WINDOWS_SETUP.md
echo.

pause