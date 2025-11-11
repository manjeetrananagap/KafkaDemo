#!/bin/bash

# Script to create Kafka topics for the demo on local Kafka installation
# Usage: ./setup-local-topics.sh [kafka-home-directory]

set -e

# Default Kafka home (adjust as needed)
KAFKA_HOME=${1:-"/usr/local/kafka"}
BOOTSTRAP_SERVER="localhost:9092"

echo "🚀 Setting up Kafka topics for demo..."
echo "Kafka Home: $KAFKA_HOME"
echo "Bootstrap Server: $BOOTSTRAP_SERVER"
echo "=================================="

# Check if Kafka home exists
if [ ! -d "$KAFKA_HOME" ]; then
    echo "❌ Kafka directory not found: $KAFKA_HOME"
    echo "Please provide the correct Kafka installation path:"
    echo "Usage: $0 /path/to/kafka"
    exit 1
fi

# Check if Kafka is running
echo "🔍 Checking if Kafka is running..."
if ! nc -z localhost 9092 2>/dev/null; then
    echo "❌ Kafka is not running on localhost:9092"
    echo "Please start Kafka first:"
    echo "  cd $KAFKA_HOME"
    echo "  bin/zookeeper-server-start.sh config/zookeeper.properties &"
    echo "  bin/kafka-server-start.sh config/server.properties &"
    exit 1
fi
echo "✅ Kafka is running"

# Function to create topic
create_topic() {
    local topic_name=$1
    local partitions=$2
    local replication_factor=$3
    
    echo "📋 Creating topic: $topic_name (partitions: $partitions, replication: $replication_factor)"
    
    $KAFKA_HOME/bin/kafka-topics.sh --create \
        --topic $topic_name \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --partitions $partitions \
        --replication-factor $replication_factor \
        --if-not-exists
    
    if [ $? -eq 0 ]; then
        echo "✅ Topic '$topic_name' created successfully"
    else
        echo "⚠️  Topic '$topic_name' might already exist"
    fi
}

# Create demo topics
echo ""
echo "📋 Creating demo topics..."

# Order events topic - main event stream
create_topic "order-events" 3 1

# Payment events topic - payment confirmations
create_topic "payment-events" 2 1

# Inventory events topic - inventory updates
create_topic "inventory-events" 2 1

# Notification events topic - notifications
create_topic "notification-events" 1 1

echo ""
echo "📊 Listing all topics:"
$KAFKA_HOME/bin/kafka-topics.sh --list --bootstrap-server $BOOTSTRAP_SERVER

echo ""
echo "🔍 Topic details:"
for topic in "order-events" "payment-events" "inventory-events" "notification-events"; do
    echo "--- $topic ---"
    $KAFKA_HOME/bin/kafka-topics.sh --describe --topic $topic --bootstrap-server $BOOTSTRAP_SERVER
    echo ""
done

echo "✅ Demo topics setup completed!"
echo ""
echo "🎯 Ready to run the demo:"
echo "  cd KafkaDemo"
echo "  mvn exec:java -Dexec.mainClass=\"com.middleware.demo.kafka.KafkaEventDrivenDemo\""
echo ""
echo "📊 To monitor topics in real-time, open separate terminals:"
echo "  # Monitor order events"
echo "  $KAFKA_HOME/bin/kafka-console-consumer.sh --topic order-events --bootstrap-server $BOOTSTRAP_SERVER --from-beginning"
echo ""
echo "  # Monitor payment events"
echo "  $KAFKA_HOME/bin/kafka-console-consumer.sh --topic payment-events --bootstrap-server $BOOTSTRAP_SERVER --from-beginning"