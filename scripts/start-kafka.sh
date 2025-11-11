#!/bin/bash

# Script to start Kafka infrastructure using Docker Compose
# This script provides options for different deployment scenarios

set -e

echo "🚀 Kafka Demo Infrastructure Startup Script"
echo "============================================="

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker first."
        exit 1
    fi
    echo "✅ Docker is running"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null 2>&1; then
        echo "❌ Docker Compose is not available. Please install Docker Compose."
        exit 1
    fi
    echo "✅ Docker Compose is available"
}

# Function to start simple Kafka setup
start_simple() {
    echo "🔧 Starting simple Kafka setup (Kafka + Zookeeper only)..."
    docker-compose -f docker-compose-simple.yml up -d
    
    echo "⏳ Waiting for Kafka to be ready..."
    sleep 30
    
    echo "✅ Simple Kafka setup is ready!"
    echo "   • Kafka broker: localhost:9092"
    echo "   • Zookeeper: localhost:2181"
}

# Function to start full Kafka setup
start_full() {
    echo "🔧 Starting full Kafka setup (with UI, Schema Registry, Connect)..."
    docker-compose up -d
    
    echo "⏳ Waiting for all services to be ready..."
    sleep 45
    
    echo "✅ Full Kafka setup is ready!"
    echo "   • Kafka broker: localhost:9092"
    echo "   • Zookeeper: localhost:2181"
    echo "   • Kafka UI: http://localhost:8080"
    echo "   • Schema Registry: http://localhost:8081"
    echo "   • Kafka Connect: http://localhost:8083"
}

# Function to create demo topics
create_topics() {
    echo "📋 Creating demo topics..."
    
    # Wait a bit more to ensure Kafka is fully ready
    sleep 10
    
    # Create order-events topic with 3 partitions
    docker exec kafka-demo-broker-simple kafka-topics --create \
        --topic order-events \
        --bootstrap-server localhost:9092 \
        --partitions 3 \
        --replication-factor 1 \
        --if-not-exists || true
    
    # Create payment-events topic
    docker exec kafka-demo-broker-simple kafka-topics --create \
        --topic payment-events \
        --bootstrap-server localhost:9092 \
        --partitions 2 \
        --replication-factor 1 \
        --if-not-exists || true
    
    echo "✅ Demo topics created successfully"
}

# Function to show status
show_status() {
    echo "📊 Kafka Infrastructure Status:"
    echo "================================"
    
    if docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -q kafka-demo; then
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep kafka-demo
        echo ""
        echo "🔍 To view topics:"
        echo "   docker exec kafka-demo-broker-simple kafka-topics --list --bootstrap-server localhost:9092"
        echo ""
        echo "🔍 To view consumer groups:"
        echo "   docker exec kafka-demo-broker-simple kafka-consumer-groups --list --bootstrap-server localhost:9092"
    else
        echo "❌ No Kafka demo containers are running"
    fi
}

# Function to stop Kafka
stop_kafka() {
    echo "🛑 Stopping Kafka infrastructure..."
    
    # Stop both simple and full setups
    docker-compose -f docker-compose-simple.yml down 2>/dev/null || true
    docker-compose down 2>/dev/null || true
    
    echo "✅ Kafka infrastructure stopped"
}

# Function to clean up (remove volumes)
cleanup() {
    echo "🧹 Cleaning up Kafka data and containers..."
    
    # Stop and remove containers with volumes
    docker-compose -f docker-compose-simple.yml down -v 2>/dev/null || true
    docker-compose down -v 2>/dev/null || true
    
    # Remove any orphaned containers
    docker container prune -f
    
    echo "✅ Cleanup completed"
}

# Main script logic
case "${1:-}" in
    "simple")
        check_docker
        check_docker_compose
        start_simple
        create_topics
        show_status
        ;;
    "full")
        check_docker
        check_docker_compose
        start_full
        show_status
        ;;
    "status")
        show_status
        ;;
    "stop")
        stop_kafka
        ;;
    "cleanup")
        cleanup
        ;;
    "topics")
        create_topics
        ;;
    *)
        echo "Usage: $0 {simple|full|status|stop|cleanup|topics}"
        echo ""
        echo "Commands:"
        echo "  simple   - Start simple Kafka setup (Kafka + Zookeeper)"
        echo "  full     - Start full Kafka setup (with UI, Schema Registry, etc.)"
        echo "  status   - Show status of running containers"
        echo "  stop     - Stop all Kafka containers"
        echo "  cleanup  - Stop containers and remove volumes"
        echo "  topics   - Create demo topics"
        echo ""
        echo "Examples:"
        echo "  $0 simple    # Start basic Kafka for demo"
        echo "  $0 status    # Check what's running"
        echo "  $0 stop      # Stop everything"
        exit 1
        ;;
esac