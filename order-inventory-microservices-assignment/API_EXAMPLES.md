# API Examples

Complete collection of API examples with request/response samples.

## Table of Contents
- [Inventory Service APIs](#inventory-service-apis)
- [Order Service APIs](#order-service-apis)
- [Error Scenarios](#error-scenarios)
- [Advanced Use Cases](#advanced-use-cases)

## Inventory Service APIs

Base URL: `http://localhost:8081`

### 1. Get Inventory Batches

**Request:**
```bash
GET /inventory/{productId}

curl http://localhost:8081/inventory/PROD-001
```

**Response (200 OK):**
```json
[
  {
    "batchNumber": "BATCH-001-B",
    "productId": "PROD-001",
    "productName": "Laptop Battery",
    "quantity": 30,
    "expiryDate": "2025-06-30",
    "manufacturingDate": "2024-06-01",
    "isExpired": false
  },
  {
    "batchNumber": "BATCH-001-A",
    "productId": "PROD-001",
    "productName": "Laptop Battery",
    "quantity": 50,
    "expiryDate": "2025-12-31",
    "manufacturingDate": "2024-01-15",
    "isExpired": false
  },
  {
    "batchNumber": "BATCH-001-C",
    "productId": "PROD-001",
    "productName": "Laptop Battery",
    "quantity": 75,
    "expiryDate": "2026-03-31",
    "manufacturingDate": "2024-09-15",
    "isExpired": false
  }
]
```

**Note:** Batches are sorted by expiry date (earliest first).

### 2. Update Inventory (FIFO - Default)

**Request:**
```bash
POST /inventory/update
Content-Type: application/json

curl -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 45
  }'
```

**Response (200 OK):**
```json
{
  "productId": "PROD-001",
  "totalQuantityDeducted": 45,
  "batchDeductions": [
    {
      "batchNumber": "BATCH-001-B",
      "quantityDeducted": 30
    },
    {
      "batchNumber": "BATCH-001-A",
      "quantityDeducted": 15
    }
  ],
  "message": "Inventory deducted successfully using FIFO strategy"
}
```

**Explanation:** Deducted from earliest expiring batch first (BATCH-001-B: 30 units), then from next batch (BATCH-001-A: 15 units).

### 3. Update Inventory (LIFO)

**Request:**
```bash
POST /inventory/update?strategy=LIFO
Content-Type: application/json

curl -X POST "http://localhost:8081/inventory/update?strategy=LIFO" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-002",
    "quantity": 100
  }'
```

**Response (200 OK):**
```json
{
  "productId": "PROD-002",
  "totalQuantityDeducted": 100,
  "batchDeductions": [
    {
      "batchNumber": "BATCH-002-A",
      "quantityDeducted": 100
    }
  ],
  "message": "Inventory deducted successfully using LIFO strategy"
}
```

**Explanation:** Deducted from latest expiring batch first.

### 4. Health Check

**Request:**
```bash
GET /inventory/health

curl http://localhost:8081/inventory/health
```

**Response (200 OK):**
```text
Inventory Service is running
```

## Order Service APIs

Base URL: `http://localhost:8082`

### 1. Place Order (Success)

**Request:**
```bash
POST /order
Content-Type: application/json

curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 10,
    "customerName": "Alice Johnson",
    "customerEmail": "alice@example.com"
  }'
```

**Response (201 Created):**
```json
{
  "orderId": "ORD-F3A8B2C1",
  "productId": "PROD-001",
  "quantity": 10,
  "customerName": "Alice Johnson",
  "customerEmail": "alice@example.com",
  "status": "CONFIRMED",
  "createdAt": "2024-11-21T14:30:15.123",
  "updatedAt": "2024-11-21T14:30:15.456",
  "message": "Order placed successfully",
  "failureReason": null
}
```

### 2. Place Order (Insufficient Inventory)

**Request:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 10000,
    "customerName": "Bob Smith",
    "customerEmail": "bob@example.com"
  }'
```

**Response (503 Service Unavailable):**
```json
{
  "timestamp": "2024-11-21T14:32:10.123",
  "status": 503,
  "error": "Inventory Service Error",
  "message": "Failed to communicate with Inventory Service: Insufficient inventory: Required: 10000, Available: 155"
}
```

**Note:** The order is still created in the database with status "FAILED".

### 3. Get Order Details

**Request:**
```bash
GET /order/{orderId}

curl http://localhost:8082/order/ORD-F3A8B2C1
```

**Response (200 OK):**
```json
{
  "orderId": "ORD-F3A8B2C1",
  "productId": "PROD-001",
  "quantity": 10,
  "customerName": "Alice Johnson",
  "customerEmail": "alice@example.com",
  "status": "CONFIRMED",
  "createdAt": "2024-11-21T14:30:15.123",
  "updatedAt": "2024-11-21T14:30:15.456",
  "message": null,
  "failureReason": null
}
```

### 4. Cancel Order (Success - Pending Order)

**Request:**
```bash
PUT /order/{orderId}/cancel

curl -X PUT http://localhost:8082/order/ORD-ABC12345/cancel
```

**Response (200 OK):**
```json
{
  "orderId": "ORD-ABC12345",
  "productId": "PROD-002",
  "quantity": 5,
  "customerName": "Charlie Brown",
  "customerEmail": "charlie@example.com",
  "status": "CANCELLED",
  "createdAt": "2024-11-21T14:30:00",
  "updatedAt": "2024-11-21T14:35:00",
  "message": "Order cancelled successfully",
  "failureReason": null
}
```

### 5. Cancel Order (Error - Confirmed Order)

**Request:**
```bash
curl -X PUT http://localhost:8082/order/ORD-F3A8B2C1/cancel
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2024-11-21T14:36:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Cannot cancel confirmed order. Order ID: ORD-F3A8B2C1"
}
```

### 6. Health Check

**Request:**
```bash
GET /order/health

curl http://localhost:8082/order/health
```

**Response (200 OK):**
```text
Order Service is running
```

## Error Scenarios

### 1. Product Not Found

**Request:**
```bash
curl http://localhost:8081/inventory/INVALID-PRODUCT
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2024-11-21T14:40:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with ID: INVALID-PRODUCT"
}
```

### 2. Order Not Found

**Request:**
```bash
curl http://localhost:8082/order/INVALID-ORDER
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2024-11-21T14:41:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: INVALID-ORDER"
}
```

### 3. Validation Errors

**Request:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "",
    "quantity": 0,
    "customerName": ""
  }'
```

**Response (400 Bad Request):**
```json
{
  "productId": "Product ID is required",
  "quantity": "Quantity must be at least 1",
  "customerName": "Customer name is required"
}
```

### 4. Invalid Email Format

**Request:**
```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 5,
    "customerName": "Test User",
    "customerEmail": "invalid-email"
  }'
```

**Response (400 Bad Request):**
```json
{
  "customerEmail": "Invalid email format"
}
```

### 5. Inventory Service Down

**Request:**
```bash
# Stop Inventory Service, then try:
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 5,
    "customerName": "Test User"
  }'
```

**Response (503 Service Unavailable):**
```json
{
  "timestamp": "2024-11-21T14:45:00",
  "status": 503,
  "error": "Inventory Service Error",
  "message": "Failed to communicate with Inventory Service: Connection refused"
}
```

## Advanced Use Cases

### 1. Complete Order Flow with Verification

```bash
# Step 1: Check initial inventory
INITIAL=$(curl -s http://localhost:8081/inventory/PROD-001 | jq '.[0].quantity')
echo "Initial quantity: $INITIAL"

# Step 2: Place order
ORDER_ID=$(curl -s -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{
    "productId":"PROD-001",
    "quantity":10,
    "customerName":"Test User"
  }' | jq -r '.orderId')
echo "Order ID: $ORDER_ID"

# Step 3: Verify order
curl -s http://localhost:8082/order/$ORDER_ID | jq

# Step 4: Check updated inventory
FINAL=$(curl -s http://localhost:8081/inventory/PROD-001 | jq '.[0].quantity')
echo "Final quantity: $FINAL"
echo "Deducted: $((INITIAL - FINAL))"
```

### 2. Bulk Orders

```bash
# Place multiple orders
for i in {1..5}; do
  curl -s -X POST http://localhost:8082/order \
    -H "Content-Type: application/json" \
    -d "{
      \"productId\":\"PROD-002\",
      \"quantity\":$((i*5)),
      \"customerName\":\"Customer $i\"
    }" | jq -r '.orderId'
  sleep 1
done
```

### 3. Test Different Strategies

```bash
# Get product with multiple batches
PRODUCT="PROD-001"

# Test FIFO
echo "=== Testing FIFO ==="
curl -s -X POST http://localhost:8081/inventory/update \
  -H "Content-Type: application/json" \
  -d "{\"productId\":\"$PRODUCT\",\"quantity\":10}" | jq

# Check remaining
curl -s http://localhost:8081/inventory/$PRODUCT | jq

# Test LIFO
echo "=== Testing LIFO ==="
curl -s -X POST "http://localhost:8081/inventory/update?strategy=LIFO" \
  -H "Content-Type: application/json" \
  -d "{\"productId\":\"$PRODUCT\",\"quantity\":10}" | jq

# Check remaining
curl -s http://localhost:8081/inventory/$PRODUCT | jq
```

### 4. Load Testing (Simple)

```bash
# Place 10 concurrent orders
for i in {1..10}; do
  (curl -s -X POST http://localhost:8082/order \
    -H "Content-Type: application/json" \
    -d '{
      "productId":"PROD-003",
      "quantity":5,
      "customerName":"Load Test User"
    }' > /dev/null) &
done
wait
echo "All orders placed"
```

## Using with HTTPie (Alternative to curl)

If you prefer HTTPie over curl:

```bash
# Install: brew install httpie

# Get inventory
http GET localhost:8081/inventory/PROD-001

# Place order
http POST localhost:8082/order \
  productId=PROD-001 \
  quantity:=10 \
  customerName="John Doe" \
  customerEmail="john@example.com"

# Update inventory
http POST localhost:8081/inventory/update \
  productId=PROD-001 \
  quantity:=20
```

## Postman Collection

Import this collection into Postman:

```json
{
  "info": {
    "name": "Order & Inventory Microservices",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Inventory",
      "request": {
        "method": "GET",
        "url": "http://localhost:8081/inventory/PROD-001"
      }
    },
    {
      "name": "Place Order",
      "request": {
        "method": "POST",
        "url": "http://localhost:8082/order",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"productId\": \"PROD-001\",\n  \"quantity\": 5,\n  \"customerName\": \"John Doe\"\n}"
        }
      }
    }
  ]
}
```

---

For more information, see [README.md](README.md) and [TESTING.md](TESTING.md).

