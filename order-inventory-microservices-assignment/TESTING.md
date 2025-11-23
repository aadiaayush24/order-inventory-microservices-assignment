# Testing Guide

Comprehensive testing guide for the Order and Inventory microservices.

## Table of Contents
- [Test Architecture](#test-architecture)
- [Running Tests](#running-tests)
- [Test Categories](#test-categories)
- [Manual Testing](#manual-testing)
- [Test Scenarios](#test-scenarios)

## Test Architecture

Both services follow a comprehensive testing strategy:

```
┌────────────────────────────────────┐
│     Integration Tests              │
│  (@SpringBootTest, MockMvc)        │
│  - Full application context        │
│  - Real database (H2)              │
│  - REST endpoint testing           │
└────────────────┬───────────────────┘
                 │
┌────────────────┴───────────────────┐
│     Unit Tests                     │
│  (JUnit 5, Mockito)                │
│  - Service layer                   │
│  - Factory pattern                 │
│  - Business logic                  │
└────────────────────────────────────┘
```

## Running Tests

### Run All Tests

```bash
# Inventory Service
cd inventory-service
./gradlew test

# Order Service
cd order-service
./gradlew test

# Run tests from project root
cd inventory-service && ./gradlew test && cd ../order-service && ./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests InventoryServiceTest
./gradlew test --tests OrderServiceTest
```

### Run Integration Tests Only

```bash
./gradlew test --tests '*IntegrationTest'
```

### Run with Coverage

```bash
./gradlew test jacocoTestReport
# View report at build/reports/jacoco/test/html/index.html
```

### Run with Verbose Output

```bash
./gradlew test --info
# or for debug level
./gradlew test --debug
```

## Test Categories

### 1. Unit Tests

#### Inventory Service Unit Tests

**InventoryServiceTest.java**
- ✅ Get inventory batches by product ID
- ✅ Throw exception when product not found
- ✅ Update inventory successfully using FIFO strategy
- ✅ Throw exception when product not found for update
- ✅ Throw exception when no available batches

**InventoryStrategyFactoryTest.java**
- ✅ Return FIFO strategy
- ✅ Return LIFO strategy
- ✅ Return default strategy for unknown type
- ✅ Case insensitive strategy selection

```bash
# Run these tests
cd inventory-service
./gradlew test --tests InventoryServiceTest
./gradlew test --tests InventoryStrategyFactoryTest
```

#### Order Service Unit Tests

**OrderServiceTest.java**
- ✅ Place order successfully
- ✅ Mark order as failed when inventory update fails
- ✅ Get order by ID successfully
- ✅ Throw exception when order not found
- ✅ Cancel pending order successfully
- ✅ Throw exception when cancelling confirmed order
- ✅ Throw exception when cancelling already cancelled order

```bash
# Run these tests
cd order-service
./gradlew test --tests OrderServiceTest
```

### 2. Integration Tests

#### Inventory Service Integration Tests

**InventoryControllerIntegrationTest.java**
- ✅ Get inventory batches for existing product
- ✅ Return 404 for non-existent product
- ✅ Update inventory successfully
- ✅ Return 400 for invalid request
- ✅ Return 400 for insufficient inventory
- ✅ Health check endpoint

```bash
cd inventory-service
./gradlew test --tests InventoryControllerIntegrationTest
```

#### Order Service Integration Tests

**OrderControllerIntegrationTest.java**
- ✅ Place order successfully
- ✅ Return 503 when inventory service fails
- ✅ Return 400 for invalid request
- ✅ Health check endpoint

```bash
cd order-service
./gradlew test --tests OrderControllerIntegrationTest
```

## Manual Testing

### Prerequisites

1. Start Inventory Service:
```bash
cd inventory-service
./gradlew bootRun
```

2. Start Order Service (in new terminal):
```bash
cd order-service
./gradlew bootRun
```

### Test Scenarios

#### Scenario 1: Successful Order Placement

**Step 1:** Check available inventory
```bash
curl http://localhost:8081/inventory/PROD-001 | jq
```

**Expected:** List of batches with quantities

**Step 2:** Place order
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":5,
    "customerName":"John Doe",
    "customerEmail":"john@example.com"
  }' | jq
```

**Expected:** Order with status "CONFIRMED"

**Step 3:** Verify inventory updated
```bash
curl http://localhost:8081/inventory/PROD-001 | jq
```

**Expected:** Quantities reduced by 5

**Step 4:** Retrieve order
```bash
curl http://localhost:8082/order/<ORDER_ID> | jq
```

#### Scenario 2: Insufficient Inventory

**Step 1:** Place order for large quantity
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":10000,
    "customerName":"Jane Doe",
    "customerEmail":"jane@example.com"
  }' | jq
```

**Expected:** 503 error with "Insufficient inventory" message

**Step 2:** Verify order is marked as FAILED
```bash
curl http://localhost:8082/order/<ORDER_ID> | jq
```

**Expected:** Order with status "FAILED" and failureReason

#### Scenario 3: Product Not Found

```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"INVALID-PRODUCT",
    "quantity":5,
    "customerName":"John Doe"
  }' | jq
```

**Expected:** 503 error with "Product not found" message

#### Scenario 4: Order Cancellation

**Step 1:** Place order
```bash
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-002",
    "quantity":5,
    "customerName":"Test User"
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.orderId')
echo "Order ID: $ORDER_ID"
```

**Step 2:** Try to cancel (should fail for CONFIRMED orders)
```bash
curl -X PUT http://localhost:8082/order/$ORDER_ID/cancel | jq
```

**Expected:** 400 error - "Cannot cancel confirmed order"

#### Scenario 5: FIFO vs LIFO Strategy

**Step 1:** Check batch order
```bash
curl http://localhost:8081/inventory/PROD-001 | jq
```

**Step 2:** Update with FIFO (default)
```bash
curl -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":10
  }' | jq
```

**Expected:** Deduction from earliest expiry batch

**Step 3:** Update with LIFO
```bash
curl -X POST "http://localhost:8081/inventory/update?strategy=LIFO" \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-002",
    "quantity":10
  }' | jq
```

**Expected:** Deduction from latest expiry batch

#### Scenario 6: Validation Errors

**Missing required fields:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "quantity":5
  }' | jq
```

**Expected:** 400 with validation errors

**Invalid quantity:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":0,
    "customerName":"Test"
  }' | jq
```

**Expected:** 400 with "Quantity must be at least 1"

## Test Data

### Sample Products (Pre-loaded in Inventory Service)

| Product ID | Name | Total Batches | Available Qty |
|------------|------|---------------|---------------|
| PROD-001 | Laptop Battery | 3 | 155 |
| PROD-002 | USB Cable | 2 | 350 |
| PROD-003 | Wireless Mouse | 2 | 180 |

### Creating Test Orders

```bash
# Small order - should succeed
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":5,"customerName":"Test User"}'

# Medium order - should succeed
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-002","quantity":50,"customerName":"Test User"}'

# Large order - should fail
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-003","quantity":500,"customerName":"Test User"}'
```

## Testing Best Practices

### Do's ✅
- Always test happy path and error scenarios
- Use meaningful test names
- Test boundary conditions
- Verify database state in integration tests
- Use test fixtures and builders
- Mock external dependencies appropriately
- Test transaction rollback scenarios

### Don'ts ❌
- Don't test Spring framework functionality
- Don't make tests dependent on each other
- Don't hardcode test data that changes
- Don't skip cleanup in tests
- Don't ignore flaky tests

## Continuous Integration

### GitHub Actions Example

```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Make gradlew executable
        run: |
          chmod +x inventory-service/gradlew
          chmod +x order-service/gradlew
      - name: Test Inventory Service
        run: cd inventory-service && ./gradlew test
      - name: Test Order Service
        run: cd order-service && ./gradlew test
```

## Test Coverage Goals

| Component | Target Coverage |
|-----------|----------------|
| Service Layer | > 90% |
| Controller Layer | > 85% |
| Repository Layer | > 80% |
| Factory Pattern | 100% |
| Exception Handling | > 90% |

## Troubleshooting Tests

### Tests Fail After Code Changes

```bash
./gradlew clean test
```

### Port Already in Use in Integration Tests

```bash
# Find and kill process
lsof -i :8081
kill -9 <PID>
```

### H2 Database Issues

```bash
# Clean and rebuild
./gradlew clean build -x test
./gradlew test
```

### Debugging Tests

```bash
# Run with debug logging
./gradlew test --debug

# Run single test method
./gradlew test --tests 'OrderServiceTest.shouldPlaceOrderSuccessfully'
```

---

**Pro Tip:** Use Swagger UI for interactive API testing:
- Inventory Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8082/swagger-ui.html

