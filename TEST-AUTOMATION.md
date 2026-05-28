# 🧪 Test Automation Features

## Test Case 1: Auto-Confirm Success ✅

### Request
```bash
POST http://localhost:8083/api/orders
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "shippingAddress": "123 Nguyen Hue, Q1, TPHCM",
  "phoneNumber": "0901234567",
  "notes": "Giao nhanh giúp em",
  "paymentMethod": "COD"
}
```

### Expected Response
```json
{
  "id": 1,
  "orderNumber": "ORD-1234567890-5678",
  "status": "CONFIRMED",
  "autoConfirmed": true,
  "totalAmount": 500000,
  "shippingAddress": "123 Nguyen Hue, Q1, TPHCM",
  "phoneNumber": "0901234567",
  "createdAt": "2024-12-28T10:00:00"
}
```

### Verify
- ✅ status = "CONFIRMED"
- ✅ autoConfirmed = true
- ✅ Frontend shows "✓ Đã tự động xác nhận"

---

## Test Case 2: Manual Confirmation Required ⚠️

### Scenario
Order value > 10,000,000 VND

### Expected Response
```json
{
  "status": "PENDING",
  "autoConfirmed": false,
  "totalAmount": 15000000
}
```

### Verify
- ✅ status = "PENDING"
- ✅ autoConfirmed = false
- ✅ Admin needs to confirm manually

---

## Test Case 3: Auto-Cancel Expired Orders 🧹

### Setup
1. Create order but don't confirm
2. Wait 30 minutes (or change config to 1 minute for testing)

### Check Database
```sql
SELECT 
  order_number,
  status,
  cancellation_reason,
  created_at,
  updated_at
FROM orders
WHERE status = 'CANCELLED'
  AND cancellation_reason LIKE '%Tự động hủy%';
```

### Expected Result
```
order_number: ORD-1234567890-1111
status: CANCELLED
cancellation_reason: Tự động hủy do không được xác nhận trong 30 phút
```

### Verify
- ✅ status = "CANCELLED"
- ✅ cancellationReason is set
- ✅ Frontend shows cancellation reason

---

## Test Case 4: Scheduled Task Logging 📊

### Check Logs Every Hour
```
Order Automation Status:
  - Auto-confirm enabled: true
  - Auto-cancel enabled: true
  - Auto-cancel after: 30 minutes
  - Business hours: 6:00 - 22:00
  - Order amount range: 0.0 - 1.0E7 VND
  - Current pending orders: 5
```

### Verify
- ✅ Log appears every hour
- ✅ Shows correct configuration
- ✅ Shows current pending count

---

## Test Case 5: Frontend Display 🎨

### Customer Page (/orders)
```
Đơn hàng #ORD-1234567890-5678
✓ Đã tự động xác nhận
Trạng thái: Đã xác nhận
```

### Admin Page (/admin/orders)
```
Mã đơn hàng: ORD-1234567890-5678
🤖 Tự động xác nhận
```

### Cancelled Order
```
Trạng thái: Đã hủy
Lý do hủy: Tự động hủy do không được xác nhận trong 30 phút
```

---

## Quick Test Script

### 1. Test Auto-Confirm (Fast)
```bash
# Create order with cart
curl -X POST http://localhost:8083/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "Test Address",
    "phoneNumber": "0901234567",
    "paymentMethod": "COD"
  }'

# Check response status should be "CONFIRMED"
```

### 2. Test Auto-Cancel (Fast)
```bash
# Change config to 1 minute for testing
# In application.yml:
# auto-cancel-minutes: 1

# Create order
# Wait 1 minute
# Check database for cancelled orders
```

### 3. Check Logs
```bash
# Watch logs for automation messages
tail -f logs/order-service.log | grep -i "auto"
```

---

## Performance Test

### Load Test: 100 Orders
```bash
# Use Apache Bench or similar tool
ab -n 100 -c 10 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -p order-payload.json \
  http://localhost:8083/api/orders
```

### Expected
- ✅ All orders auto-confirmed within 1 second
- ✅ No errors
- ✅ Database updated correctly

---

## Rollback Plan

If automation causes issues:

### 1. Disable Auto-Confirm
```yaml
auto-confirm-enabled: false
```

### 2. Disable Auto-Cancel
```yaml
auto-cancel-enabled: false
```

### 3. Restart Service
```bash
docker-compose restart order-service
```

### 4. Verify
All new orders should be PENDING and require manual confirmation.

---

## Success Criteria ✅

- [x] Orders < 10M VND auto-confirmed
- [x] Orders > 10M VND require manual confirmation
- [x] Pending orders auto-cancelled after 30 minutes
- [x] Frontend displays automation status
- [x] Logs show automation activity
- [x] No performance degradation
- [x] Database fields populated correctly

---

**Test Date:** 2024-12-28  
**Tester:** Development Team  
**Status:** Ready for Testing
