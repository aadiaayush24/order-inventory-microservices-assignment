# Inventory Service

A Spring Boot microservice for managing product inventory with batch tracking and expiry date management.

## Features

- Product and inventory batch management
- Batch expiry date tracking
- Automatic sorting by expiry date (FEFO strategy)
- Factory Design Pattern for extensible inventory deduction strategies
- RESTful API with OpenAPI documentation
- H2 in-memory database
- Comprehensive unit and integration tests

## Tech Stack

- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 & Mockito

## Running the Service

```bash
# Using Gradle
./gradlew bootRun

# Using JAR
./gradlew bootJar
java -jar build/libs/inventory-service-1.0.0.jar
```

The service will start on **port 8081**.

## API Endpoints

### 1. Get Inventory Batches
```
GET /inventory/{productId}
```
Returns all inventory batches for a product, sorted by expiry date (earliest first).

**Example:**
```bash
curl http://localhost:8081/inventory/PROD-001
```

### 2. Update Inventory
```
POST /inventory/update?strategy=FIFO
Content-Type: application/json

{
  "productId": "string",
  "quantity": number
}
```

Deducts inventory quantity. Supports multiple strategies:
- **FIFO** (First In First Out) - Default
- **LIFO** (Last In First Out)

**Example:**
```bash
curl -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":10}'
```

### 3. Health Check
```
GET /inventory/health
```

## Factory Design Pattern

The service implements the Factory Pattern for inventory management strategies:

```
InventoryStrategyFactory
    ├── FifoInventoryStrategy (Default)
    ├── LifoInventoryStrategy
    └── [Future strategies can be added here]
```

### Adding New Strategies

1. Implement `InventoryStrategy` interface
2. Annotate with `@Component`
3. Implement `deductInventory()` method
4. The factory automatically registers it

**Example:**
```java
@Component
public class FefoInventoryStrategy implements InventoryStrategy {
    
    @Override
    public InventoryUpdateResponse deductInventory(List<InventoryBatch> batches, int quantity) {
        // Implementation
    }
    
    @Override
    public String getStrategyType() {
        return "FEFO";
    }
}
```

## Database

### H2 Console
Access at: http://localhost:8081/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (empty)

### Sample Data

The service initializes with sample data (see `data.sql`):
- 3 Products
- 7 Inventory Batches

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests InventoryServiceTest

# Run integration tests
./gradlew test --tests '*IntegrationTest'
```

### Test Coverage
- Unit tests for service layer
- Factory pattern tests
- Integration tests for REST endpoints
- Repository tests

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
server:
  port: 8081  # Change port if needed

spring:
  datasource:
    url: jdbc:h2:mem:inventorydb  # Database URL
  jpa:
    show-sql: true  # Show SQL queries in logs
```

## API Documentation

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8081/api-docs

## Error Handling

The service provides comprehensive error responses:

```json
{
  "timestamp": "2024-11-21T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with ID: PROD-999"
}
```

## Logging

Logs are configured with DEBUG level for development:
- SQL queries
- Service operations
- Strategy selections

## Project Structure

```
src/
├── main/
│   ├── java/com/inventory/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Data access
│   │   ├── model/            # JPA entities
│   │   ├── factory/          # Factory pattern implementation
│   │   ├── dto/              # Data transfer objects
│   │   └── exception/        # Exception handling
│   └── resources/
│       ├── application.yml   # Configuration
│       └── data.sql          # Sample data
└── test/
    ├── java/                 # Unit & integration tests
    └── resources/            # Test configuration
```

## Future Enhancements

- Add batch creation endpoints
- Implement product CRUD operations
- Add inventory alerts for low stock
- Implement expiry date notifications
- Add audit logging
- Support for multiple warehouses
- Batch transfer between locations

---

For more information, see the main [README.md](../README.md) in the project root.

