# Order & Inventory Microservices Assignment

A professional implementation of two Spring Boot microservices demonstrating inter-service communication, factory design pattern, and comprehensive testing.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Running the Services](#running-the-services)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Design Patterns](#design-patterns)
- [Build System](#build-system)
- [Future Enhancements](#future-enhancements)

## Overview

This project consists of two microservices:

1. **Inventory Service** - Manages product inventory with batch tracking and expiry date management
2. **Order Service** - Handles customer orders and communicates with Inventory Service to update stock

### Key Features

- **Inventory Service:**
  - Product and batch management with expiry date tracking
  - Factory Design Pattern for extensible inventory deduction strategies (FIFO, LIFO)
  - RESTful APIs for inventory queries and updates
  - Batch sorting by expiry date (FEFO - First Expiry First Out)

- **Order Service:**
  - Order creation and management
  - Non-blocking HTTP communication with Inventory Service using WebClient
  - Automatic order status management (PENDING → CONFIRMED/FAILED)
  - Order cancellation support

## Architecture

```
┌─────────────────┐          HTTP/REST          ┌──────────────────┐
│                 │  ─────────────────────────▶  │                  │
│  Order Service  │                              │ Inventory Service│
│    (Port 8082)  │  ◀─────────────────────────  │    (Port 8081)   │
└─────────────────┘                              └──────────────────┘
        │                                                  │
        │                                                  │
    ┌───▼───┐                                          ┌───▼───┐
    │  H2   │                                          │  H2   │
    │  DB   │                                          │  DB   │
    └───────┘                                          └───────┘
```

### Layer Architecture

Both services follow a clean architecture pattern:

```
Controller Layer → Service Layer → Repository Layer → Database
                        ↓
                  Factory Pattern (Inventory Service)
                        ↓
              Strategy Implementations
```

## Technologies Used

- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Spring WebFlux** - Non-blocking HTTP client (Order Service)
- **Lombok** - Boilerplate code reduction
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Gradle** - Build tool

## Prerequisites

- **Java 17** or higher
- **Gradle 8.5+** (or use included wrapper)
- **curl** or **Postman** (for API testing)

## Project Structure

```
order-inventory-microservices-assignment/
├── inventory-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/inventory/
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── service/          # Business logic
│   │   │   │   ├── repository/       # Data access
│   │   │   │   ├── model/            # Entity classes
│   │   │   │   ├── factory/          # Factory pattern implementation
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   ├── exception/        # Exception handling
│   │   │   │   └── InventoryServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml   # Configuration
│   │   │       └── data.sql          # Sample data
│   │   └── test/                     # Unit & integration tests
│   └── pom.xml
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/order/
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── service/          # Business logic
│   │   │   │   ├── repository/       # Data access
│   │   │   │   ├── model/            # Entity classes
│   │   │   │   ├── client/           # HTTP clients
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── exception/        # Exception handling
│   │   │   │   └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml   # Configuration
│   │   └── test/                     # Unit & integration tests
│   ├── build.gradle              # Gradle build configuration
│   ├── settings.gradle           # Gradle settings
│   ├── gradlew                   # Gradle wrapper (Unix/Mac)
│   └── gradlew.bat               # Gradle wrapper (Windows)
└── README.md
```

## Setup Instructions

### 1. Clone or Extract the Project

```bash
cd order-inventory-microservices-assignment
```

### 2. Build Both Services

```bash
# Build Inventory Service
cd inventory-service
./gradlew build
cd ..

# Build Order Service
cd order-service
./gradlew build
cd ..
```

**Note:** The first time you run `./gradlew`, it will download Gradle automatically.

## Running the Services

### Option 1: Run with Gradle

**Terminal 1 - Inventory Service:**
```bash
cd inventory-service
./gradlew bootRun
```

**Terminal 2 - Order Service:**
```bash
cd order-service
./gradlew bootRun
```

### Option 2: Run JAR Files

```bash
# Build JARs first
cd inventory-service && ./gradlew bootJar && cd ..
cd order-service && ./gradlew bootJar && cd ..

# Terminal 1
java -jar inventory-service/build/libs/inventory-service-1.0.0.jar

# Terminal 2
java -jar order-service/build/libs/order-service-1.0.0.jar
```

### Verify Services are Running

```bash
# Check Inventory Service
curl http://localhost:8081/inventory/health

# Check Order Service
curl http://localhost:8082/order/health
```

## API Documentation

### Inventory Service (Port 8081)

**Swagger UI:** http://localhost:8081/swagger-ui.html  
**OpenAPI JSON:** http://localhost:8081/api-docs  
**H2 Console:** http://localhost:8081/h2-console

#### Endpoints:

**1. Get Inventory Batches**
```bash
GET /inventory/{productId}

Example:
curl http://localhost:8081/inventory/PROD-001
```

**Response:**
```json
[
  {
    "batchNumber": "BATCH-001-A",
    "productId": "PROD-001",
    "productName": "Laptop Battery",
    "quantity": 50,
    "expiryDate": "2025-12-31",
    "manufacturingDate": "2024-01-15",
    "isExpired": false
  }
]
```

**2. Update Inventory**
```bash
POST /inventory/update
Content-Type: application/json

{
  "productId": "PROD-001",
  "quantity": 10
}

Example:
curl -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":10}'
```

**Response:**
```json
{
  "productId": "PROD-001",
  "totalQuantityDeducted": 10,
  "batchDeductions": [
    {
      "batchNumber": "BATCH-001-B",
      "quantityDeducted": 10
    }
  ],
  "message": "Inventory deducted successfully using FIFO strategy"
}
```

**3. Update Inventory with Strategy**
```bash
POST /inventory/update?strategy=LIFO
Content-Type: application/json

{
  "productId": "PROD-001",
  "quantity": 5
}
```

### Order Service (Port 8082)

**Swagger UI:** http://localhost:8082/swagger-ui.html  
**OpenAPI JSON:** http://localhost:8082/api-docs  
**H2 Console:** http://localhost:8082/h2-console

#### Endpoints:

**1. Place Order**
```bash
POST /order
Content-Type: application/json

{
  "productId": "PROD-001",
  "quantity": 5,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com"
}

Example:
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":5,
    "customerName":"John Doe",
    "customerEmail":"john.doe@example.com"
  }'
```

**Response:**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "productId": "PROD-001",
  "quantity": 5,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "status": "CONFIRMED",
  "createdAt": "2024-11-21T10:30:00",
  "updatedAt": "2024-11-21T10:30:01",
  "message": "Order placed successfully",
  "failureReason": null
}
```

**2. Get Order**
```bash
GET /order/{orderId}

Example:
curl http://localhost:8082/order/ORD-A1B2C3D4
```

**3. Cancel Order**
```bash
PUT /order/{orderId}/cancel

Example:
curl -X PUT http://localhost:8082/order/ORD-A1B2C3D4/cancel
```

### Sample Data

The Inventory Service comes pre-loaded with sample data:

| Product ID | Product Name | Total Batches | Total Quantity |
|------------|--------------|---------------|----------------|
| PROD-001   | Laptop Battery | 3 | 155 |
| PROD-002   | USB Cable | 2 | 350 |
| PROD-003   | Wireless Mouse | 2 | 180 |

## Testing

### Run All Tests

```bash
# Inventory Service tests
cd inventory-service
./gradlew test

# Order Service tests
cd order-service
./gradlew test
```

### Test Coverage

Both services include:

- **Unit Tests** - Testing individual components with Mockito
  - Service layer tests
  - Factory pattern tests
  - Repository tests

- **Integration Tests** - Testing complete workflows with @SpringBootTest
  - Controller integration tests
  - End-to-end API tests
  - Database integration tests

### Example Test Execution

```bash
# Run tests with detailed output
./gradlew test --tests InventoryServiceTest

# Run integration tests only
./gradlew test --tests '*IntegrationTest'

# Run tests with info logging
./gradlew test --info
```

## Build System

This project uses **Gradle 8.5** with the included Gradle Wrapper - no installation required!

### Quick Commands:
```bash
./gradlew build      # Build project
./gradlew bootRun    # Run application
./gradlew test       # Run tests
./gradlew clean      # Clean build directory
```

📖 **For complete build system documentation, see [BUILD_SYSTEM.md](BUILD_SYSTEM.md)**

## Design Patterns

### Factory Design Pattern (Inventory Service)

The Inventory Service implements the Factory Pattern to support multiple inventory deduction strategies:

**Components:**

1. **InventoryStrategy** (Interface)
   - Defines contract for inventory deduction strategies

2. **Concrete Strategies:**
   - **FifoInventoryStrategy** - First In First Out (default)
   - **LifoInventoryStrategy** - Last In First Out
   - Easily extensible for new strategies (FEFO, Weighted Average, etc.)

3. **InventoryStrategyFactory**
   - Creates and manages strategy instances
   - Provides strategy selection based on type

**Benefits:**
- **Extensibility** - New strategies can be added without modifying existing code
- **Maintainability** - Each strategy is isolated and independently testable
- **Flexibility** - Strategy selection at runtime

**Usage Example:**
```java
// Service automatically uses factory to get appropriate strategy
inventoryService.updateInventory(request, "FIFO");  // Uses FIFO
inventoryService.updateInventory(request, "LIFO");  // Uses LIFO
```

## Future Enhancements

### Potential Improvements:

1. **Service Discovery** - Implement Eureka or Consul for dynamic service discovery
2. **API Gateway** - Add Spring Cloud Gateway for unified entry point
3. **Circuit Breaker** - Implement Resilience4j for fault tolerance
4. **Caching** - Add Redis for inventory caching
5. **Message Queue** - Use RabbitMQ or Kafka for asynchronous communication
6. **Monitoring** - Add Prometheus and Grafana for metrics
7. **Distributed Tracing** - Implement Sleuth and Zipkin
8. **Security** - Add Spring Security with OAuth2/JWT
9. **Database** - Migrate to PostgreSQL or MySQL for production
10. **Containerization** - Create Docker images and Kubernetes deployments

### Additional Inventory Strategies:

- **FEFO** (First Expiry First Out) - Already sorted by expiry date
- **Weighted Average** - Based on cost or priority
- **Zone-based** - Deduct from specific warehouse zones
- **Batch-specific** - Allow manual batch selection

## Architecture Decisions

### Why WebClient over RestTemplate?

- Non-blocking I/O for better performance
- Better support for reactive programming
- RestTemplate is in maintenance mode

### Why H2 Database?

- Quick setup for demonstration
- In-memory for easy testing
- Easy to migrate to production database

### Why Factory Pattern?

- Aligns with Open/Closed Principle (SOLID)
- Makes inventory strategies pluggable
- Simplifies testing of individual strategies

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8081 or 8082
lsof -i :8081
lsof -i :8082

# Kill the process
kill -9 <PID>
```

### Connection Refused

- Ensure Inventory Service is running before starting Order Service
- Check that services are running on correct ports
- Verify `inventory.service.url` in Order Service configuration

### Tests Failing

```bash
# Clean and rebuild
./gradlew clean build -x test
./gradlew test
```

## Contributors

This project was developed as a Spring Boot microservices assignment demonstrating:
- Clean architecture
- Design patterns
- Inter-service communication
- Comprehensive testing
- Professional documentation

## License

This project is for educational purposes.

---

**Built with ❤️ using Spring Boot**

