#!/bin/bash

echo "🚀 Starting Hansal Verrechnungsprogramm v3..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install it first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

echo ""
echo "🏗️  Building and starting services..."
echo "   - PostgreSQL Database"
echo "   - Spring Boot Backend"
echo "   - Angular Frontend"
echo ""

# Build and start all services
docker-compose up -d --build

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo ""
echo "🔍 Checking service status..."
docker-compose ps

echo ""
echo "✅ Application is starting!"
echo ""
echo "📡 Access URLs:"
echo "   Frontend:  http://localhost"
echo "   Backend:   http://localhost:8080/api"
echo "   Health:    http://localhost:8080/actuator/health"
echo ""
echo "📋 Useful commands:"
echo "   View logs:        docker-compose logs -f"
echo "   Stop services:    docker-compose down"
echo "   Restart:          docker-compose restart"
echo ""
echo "🎉 Setup complete! The application should be ready in a few moments."
