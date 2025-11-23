# Order Service

A Spring Boot microservice for managing customer orders with automatic inventory integration.

## Features

- Order creation and management
- Automatic inventory validation and updates
- Order status tracking (PENDING → CONFIRMED/FAILED)
- Order cancellation
- Non-blocking HTTP communication with Inventory Service
- RESTful API with OpenAPI documentation
- H2 in-memory database
- Comprehensive unit and integration tests

## Tech Stack

- Spring Boot 3.2.0
- Spring Data JPA
- Spring WebFlux (WebClient)
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
java -jar build/libs/order-service-1.0.0.jar
```

The service will start on **port 8082**.

**Note:** Inventory Service must be running on port 8081 for order placement to work.

## API Endpoints

### 1. Place Order
```
POST /order
Content-Type: application/json

{
  "productId": "string",
  "quantity": number,
  "customerName": "string",
  "customerEmail": "string (optional)"
}
```

Creates a new order and automatically updates inventory.

**Example:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":5,
    "customerName":"John Doe",
    "customerEmail":"john.doe@example.com"
  }'
```

**Success Response (201):**
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

**Failure Response (503):**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "productId": "PROD-001",
  "quantity": 1000,
  "status": "FAILED",
  "failureReason": "Insufficient inventory: Required 1000, Available: 100"
}
```

### 2. Get Order
```
GET /order/{orderId}
```

Retrieves order details by order ID.

**Example:**
```bash
curl http://localhost:8082/order/ORD-A1B2C3D4
```

### 3. Cancel Order
```
PUT /order/{orderId}/cancel
```

Cancels a pending or failed order. Cannot cancel confirmed orders.

**Example:**
```bash
curl -X PUT http://localhost:8082/order/ORD-A1B2C3D4/cancel
```

### 4. Health Check
```
GET /order/health
```

## Order Lifecycle

```
┌─────────┐
│ PENDING │ ─────┐
└─────────┘      │
                 │
        ┌────────┴─────────┐
        │                  │
        ▼                  ▼
┌───────────┐      ┌──────────┐
│ CONFIRMED │      │  FAILED  │
└───────────┘      └──────────┘
        │                  │
        │                  │
        └────────┬─────────┘
                 │
                 ▼
          ┌──────────┐
          │CANCELLED │
          └──────────┘
```

**Status Descriptions:**
- **PENDING** - Order created, awaiting inventory update
- **CONFIRMED** - Inventory successfully updated, order confirmed
- **FAILED** - Inventory update failed (insufficient stock, product not found, etc.)
- **CANCELLED** - Order manually cancelled (only for PENDING/FAILED orders)

## Integration with Inventory Service

The Order Service communicates with Inventory Service using:

**WebClient** (Non-blocking HTTP client)
- Timeout: 5 seconds
- Automatic error handling
- Retry logic (can be configured)

**Configuration:**
```yaml
inventory:
  service:
    url: http://localhost:8081
```

## Database

### H2 Console
Access at: http://localhost:8082/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (empty)

### Schema

**orders** table:
- id (PK)
- order_id (unique)
- product_id
- quantity
- customer_name
- customer_email
- status
- created_at
- updated_at
- failure_reason

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests OrderServiceTest

# Run integration tests
./gradlew test --tests '*IntegrationTest'
```

### Test Coverage
- Unit tests for service layer
- Integration tests with mocked Inventory Service
- REST endpoint tests
- Order lifecycle tests

## API Documentation

- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8082/api-docs

## Error Handling

The service provides comprehensive error responses:

**404 - Order Not Found:**
```json
{
  "timestamp": "2024-11-21T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: ORD-INVALID"
}
```

**503 - Inventory Service Error:**
```json
{
  "timestamp": "2024-11-21T10:30:00",
  "status": 503,
  "error": "Inventory Service Error",
  "message": "Failed to communicate with Inventory Service"
}
```

**400 - Validation Error:**
```json
{
  "productId": "Product ID is required",
  "quantity": "Quantity must be at least 1",
  "customerName": "Customer name is required"
}
```

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8082  # Change port if needed

inventory:
  service:
    url: http://localhost:8081  # Inventory Service URL

spring:
  datasource:
    url: jdbc:h2:mem:orderdb
```

## Logging

Logs include:
- Order creation and status changes
- Inventory Service communication
- Error details and stack traces

## Project Structure

```
src/
├── main/
│   ├── java/com/order/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Data access
│   │   ├── model/            # JPA entities
│   │   ├── client/           # HTTP clients
│   │   ├── dto/              # Data transfer objects
│   │   ├── config/           # Configuration classes
│   │   └── exception/        # Exception handling
│   └── resources/
│       └── application.yml   # Configuration
└── test/
    ├── java/                 # Unit & integration tests
    └── resources/            # Test configuration
```

## Troubleshooting

### Connection Refused Error
**Problem:** Order Service cannot connect to Inventory Service

**Solution:**
1. Ensure Inventory Service is running: `curl http://localhost:8081/inventory/health`
2. Check `inventory.service.url` in application.yml
3. Verify no firewall blocking port 8081

### Order Stuck in PENDING
**Problem:** Orders remain in PENDING status

**Solution:**
1. Check Inventory Service logs
2. Verify product exists in inventory
3. Check network connectivity between services

### Tests Failing
**Problem:** Integration tests fail

**Solution:**
```bash
./gradlew clean build -x test
./gradlew test
```

## Future Enhancements

- Payment integration
- Order history queries
- Order modifications
- Partial fulfillment support
- Email notifications
- Order tracking
- Inventory reservation (2-phase commit)
- Async order processing with message queues
- Order analytics dashboard

## Best Practices Implemented

✅ Clean Architecture (Controller → Service → Repository)  
✅ Comprehensive error handling  
✅ Validation at API layer  
✅ Transaction management  
✅ Non-blocking HTTP communication  
✅ Proper status management  
✅ Extensive test coverage  
✅ API documentation  
✅ Logging and monitoring hooks  

---

For more information, see the main [README.md](../README.md) in the project root.

