# Quick Start Guide

Get up and running with the Order & Inventory microservices in 5 minutes!

## Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Note: Gradle wrapper is included, no need to install Gradle
```

## Step 1: Build the Services (2 minutes)

```bash
# Navigate to project directory
cd order-inventory-microservices-assignment

# Build Inventory Service
cd inventory-service
./gradlew build
cd ..

# Build Order Service
cd order-service
./gradlew build
cd ..
```

**Note:** The first time you run `./gradlew`, it will automatically download Gradle.

## Step 2: Start the Services (1 minute)

Open **two terminal windows**:

**Terminal 1 - Inventory Service:**
```bash
cd inventory-service
./gradlew bootRun
```

Wait until you see: `Started InventoryServiceApplication`

**Terminal 2 - Order Service:**
```bash
cd order-service
./gradlew bootRun
```

Wait until you see: `Started OrderServiceApplication`

## Step 3: Verify Services are Running (30 seconds)

```bash
# Check Inventory Service
curl http://localhost:8081/inventory/health

# Check Order Service
curl http://localhost:8082/order/health
```

Both should return: `"... Service is running"`

## Step 4: Try Your First API Calls (1 minute)

### View Available Inventory

```bash
curl http://localhost:8081/inventory/PROD-001 | jq
```

You should see a list of inventory batches with quantities.

### Place Your First Order

```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 5,
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com"
  }' | jq
```

You should see an order response with status `CONFIRMED` and an order ID like `ORD-A1B2C3D4`.

### Verify Inventory Was Updated

```bash
curl http://localhost:8081/inventory/PROD-001 | jq
```

The quantities should be reduced by 5!

## Step 5: Explore with Swagger UI (30 seconds)

Open in your browser:
- **Inventory Service:** http://localhost:8081/swagger-ui.html
- **Order Service:** http://localhost:8082/swagger-ui.html

Try the interactive API documentation!

## Common Commands

### View H2 Database Console

**Inventory Service:**
- URL: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (leave empty)

**Order Service:**
- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (leave empty)

### Stop the Services

Press `Ctrl+C` in each terminal window.

## Quick Test Scenarios

### Test FIFO Strategy (First In First Out)

```bash
# This will deduct from the earliest expiring batch first
curl -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-002","quantity":20}' | jq
```

### Test LIFO Strategy (Last In First Out)

```bash
# This will deduct from the latest expiring batch first
curl -X POST "http://localhost:8081/inventory/update?strategy=LIFO" \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-003","quantity":15}' | jq
```

### Test Insufficient Inventory

```bash
# Try to order more than available
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 10000,
    "customerName": "Jane Doe"
  }' | jq
```

You should get an error about insufficient inventory, and the order will have status `FAILED`.

## Sample Products Available

| Product ID | Name | Available Quantity |
|------------|------|-------------------|
| PROD-001 | Laptop Battery | 155 units |
| PROD-002 | USB Cable | 350 units |
| PROD-003 | Wireless Mouse | 180 units |

## Next Steps

1. **Read the Full Documentation:** [README.md](README.md)
2. **Learn About Testing:** [TESTING.md](TESTING.md)
3. **Explore the Code:**
   - Inventory Service: `inventory-service/src/main/java/com/inventory/`
   - Order Service: `order-service/src/main/java/com/order/`

## Troubleshooting

### "Port 8081 already in use"

```bash
# Find and kill the process
lsof -i :8081
kill -9 <PID>
```

### "Connection refused" in Order Service

Make sure Inventory Service is running first on port 8081.

### Services not starting

```bash
# Try clean build
cd inventory-service
./gradlew clean build
cd ../order-service
./gradlew clean build
```

## Quick Architecture Overview

```
┌─────────────────────┐
│   Order Service     │
│   (Port 8082)       │
│                     │
│ • Place Orders      │
│ • Track Status      │
│ • Cancel Orders     │
└──────────┬──────────┘
           │
           │ REST API Call
           ▼
┌─────────────────────┐
│ Inventory Service   │
│   (Port 8081)       │
│                     │
│ • Manage Inventory  │
│ • Track Batches     │
│ • FIFO/LIFO Logic   │
└─────────────────────┘
```

## Happy Coding! 🚀

For more details, see:
- [Main README](README.md)
- [Testing Guide](TESTING.md)
- [Inventory Service README](inventory-service/README.md)
- [Order Service README](order-service/README.md)

