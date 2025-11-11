#!/bin/bash

# Script to monitor Kafka topics during demo presentation
# Usage: ./monitor-demo.sh [kafka-home-directory] [topic-name]

set -e

KAFKA_HOME=${1:-"/usr/local/kafka"}
TOPIC=${2:-"order-events"}
BOOTSTRAP_SERVER="localhost:9092"

echo "🔍 Kafka Demo Monitor"
echo "===================="
echo "Kafka Home: $KAFKA_HOME"
echo "Topic: $TOPIC"
echo "Bootstrap Server: $BOOTSTRAP_SERVER"
echo ""

# Check if Kafka home exists
if [ ! -d "$KAFKA_HOME" ]; then
    echo "❌ Kafka directory not found: $KAFKA_HOME"
    echo "Usage: $0 /path/to/kafka [topic-name]"
    exit 1
fi

# Check if Kafka is running
if ! nc -z localhost 9092 2>/dev/null; then
    echo "❌ Kafka is not running on localhost:9092"
    exit 1
fi

echo "🎯 Monitoring topic: $TOPIC"
echo "Press Ctrl+C to stop monitoring"
echo "================================"
echo ""

# Monitor the topic with enhanced formatting
$KAFKA_HOME/bin/kafka-console-consumer.sh \
    --topic $TOPIC \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --from-beginning \
    --property print.headers=true \
    --property print.timestamp=true \
    --property print.partition=true \
    --property print.offset=true \
    --property print.key=true \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property key.separator=" | " \
    --property line.separator="\n---\n"