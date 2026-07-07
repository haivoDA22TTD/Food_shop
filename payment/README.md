# Payment Service

Payment Service cho hệ thống Food Shop - Quản lý thanh toán và voucher với **Saga Pattern** để đảm bảo data consistency.

## 🎯 Tính năng

### Payment Management
- ✅ Tạo payment từ order
- ✅ Hỗ trợ nhiều phương thức thanh toán (COD, VNPay, MoMo, Bank Transfer)
- ✅ Xử lý callback từ payment gateway
- ✅ Auto-cancel expired payments
- ✅ Refund management
- ✅ Payment statistics
- ✅ **Saga Pattern** cho distributed transactions

### Voucher Management
- ✅ Tạo và quản lý voucher
- ✅ Hỗ trợ nhiều loại voucher (Percentage, Fixed Amount, Free Shipping)
- ✅ Giới hạn số lần sử dụng (global và per-user)
- ✅ Validate voucher theo điều kiện
- ✅ Track voucher usage
- ✅ Redis caching cho performance

### Saga Pattern (NEW! 🔄)
- ✅ Orchestration-based Saga
- ✅ Automatic compensation on failure
- ✅ Retry mechanism with configurable max retries
- ✅ Saga recovery for stale transactions
- ✅ Complete audit trail
- ✅ Admin monitoring endpoints

## 🏗️ Kiến trúc

```
Payment-Service/
├── entity/          - Payment, Voucher, VoucherUsage, PaymentSaga
├── repository/      - JPA Repositories
├── service/         - Business logic
│   ├── PaymentService
│   ├── VoucherService
│   ├── VNPayService
│   └── OrderFeignClient
├── saga/            - Saga Pattern implementation (NEW!)
│   ├── PaymentSagaOrchestrator
│   ├── SagaRecoveryService
│   ├── SagaStatus
│   └── SagaStep
├── controller/      - REST APIs
│   ├── PaymentController
│   ├── VoucherController
│   ├── AdminPaymentController
│   └── SagaAdminController (NEW!)
├── dto/             - Request/Response DTOs
├── config/          - Configuration classes
├── security/        - JWT Authentication
└── exception/       - Exception handling
```

## 📊 Database Schema

### Payments Table
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_number VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    voucher_id BIGINT,
    voucher_code VARCHAR(50),
    transaction_id VARCHAR(100),
    payment_gateway_response TEXT,
    payment_url TEXT,
    paid_at DATETIME,
    expired_at DATETIME,
    refund_reason TEXT,
    refunded_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

### Vouchers Table
```sql
CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    voucher_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    max_discount_amount DECIMAL(10,2),
    min_order_amount DECIMAL(10,2) DEFAULT 0,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    per_user_limit INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

### Voucher Usages Table
```sql
CREATE TABLE voucher_usages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_id BIGINT,
    discount_amount DECIMAL(10,2) NOT NULL,
    used_at DATETIME NOT NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id)
);
```

### Payment Sagas Table (NEW! 🔄)
```sql
CREATE TABLE payment_sagas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    saga_id VARCHAR(50) UNIQUE NOT NULL,
    payment_id BIGINT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_step VARCHAR(30),
    voucher_id BIGINT,
    error_message TEXT,
    compensation_count INT DEFAULT 0,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    saga_data TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    completed_at DATETIME,
    INDEX idx_saga_id (saga_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status)
);
```

## 🔌 API Endpoints

### User Endpoints

#### Payments
```
POST   /api/payments                    - Tạo payment mới
GET    /api/payments/{id}               - Lấy thông tin payment
GET    /api/payments                    - Danh sách payments của user
GET    /api/payments/order/{orderId}    - Lấy payment theo order
GET    /api/payments/vnpay-callback     - VNPay callback
POST   /api/payments/{id}/cancel        - Hủy payment
```

#### Vouchers
```
GET    /api/vouchers                    - Danh sách voucher khả dụng
POST   /api/vouchers/validate           - Validate voucher
GET    /api/vouchers/my-usages          - Lịch sử sử dụng voucher
```

### Admin Endpoints

#### Payments
```
GET    /api/admin/payments              - Danh sách tất cả payments
GET    /api/admin/payments/{id}         - Chi tiết payment
GET    /api/admin/payments/statistics   - Thống kê payments
POST   /api/admin/payments/{id}/refund  - Hoàn tiền
```

#### Vouchers
```
POST   /api/admin/vouchers              - Tạo voucher
PUT    /api/admin/vouchers/{id}         - Cập nhật voucher
DELETE /api/admin/vouchers/{id}         - Xóa voucher
GET    /api/admin/vouchers              - Danh sách vouchers
GET    /api/admin/vouchers/{id}         - Chi tiết voucher
GET    /api/admin/vouchers/{id}/usages  - Lịch sử sử dụng voucher
```

#### Sagas (NEW! 🔄)
```
GET    /api/admin/sagas                 - Danh sách tất cả sagas
GET    /api/admin/sagas/{id}            - Chi tiết saga
GET    /api/admin/sagas/order/{orderId} - Saga theo order
GET    /api/admin/sagas/payment/{paymentId} - Saga theo payment
GET    /api/admin/sagas/statistics      - Thống kê sagas
GET    /api/admin/sagas/active          - Danh sách active sagas
GET    /api/admin/sagas/failed          - Danh sách failed sagas
POST   /api/admin/sagas/{id}/retry      - Retry saga
POST   /api/admin/sagas/{id}/compensate - Compensate saga
POST   /api/admin/sagas/recovery/trigger - Trigger recovery
```

## 🚀 Cài đặt và Chạy

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Eureka Server đang chạy

### Configuration

#### Local Development (application.yml)
```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_shop_db
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: your-secret-key-must-be-at-least-32-characters-long

vnpay:
  tmn-code: YOUR_VNPAY_TMN_CODE
  hash-secret: YOUR_VNPAY_HASH_SECRET
  url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  return-url: http://localhost:3000/payment/callback
```

#### Production (application-prod.yml)
Sử dụng environment variables:
- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `VNPAY_TMN_CODE`
- `VNPAY_HASH_SECRET`
- `VNPAY_RETURN_URL`

### Build và Run

```bash
# Build
mvn clean package

# Run local
mvn spring-boot:run

# Run production
java -jar -Dspring.profiles.active=prod target/app.jar
```

## 🔐 Security

- JWT Authentication
- Role-based access control (USER, ADMIN)
- CORS configuration
- Request validation
- Secure payment gateway integration

## 📝 Payment Flow

### COD (Cash on Delivery) with Saga Pattern
1. User tạo payment với method = COD
2. **Saga starts** (SAGA-xxx)
3. **Step 1**: Validate order exists và belongs to user
4. **Step 2**: Reserve voucher (if applicable)
5. **Step 3**: Create payment record
6. **Step 4**: Process payment (auto-complete for COD)
7. **Step 5**: Confirm order status
8. **Saga completes** ✓
9. Payment status = COMPLETED
10. Order status = CONFIRMED

### Online Payment (VNPay, MoMo) with Saga Pattern
1. User tạo payment với method = VNPAY/MOMO
2. **Saga starts** (SAGA-xxx)
3. **Step 1**: Validate order
4. **Step 2**: Reserve voucher (if applicable)
5. **Step 3**: Create payment record
6. **Step 4**: Generate payment URL
7. User redirect đến payment gateway
8. User thanh toán
9. Gateway callback về system
10. System validate và update payment status
11. **Step 5**: Confirm order (if payment successful)
12. **Saga completes** ✓

### Saga Compensation (on failure)
1. Any step fails
2. **Saga starts compensation**
3. Rollback in reverse order:
   - Cancel payment (if created)
   - Release voucher (if reserved)
4. **Saga compensated** ✓
5. User notified of failure

## 🎫 Voucher System

### Voucher Types
- **PERCENTAGE**: Giảm theo phần trăm (VD: 10%)
- **FIXED_AMOUNT**: Giảm số tiền cố định (VD: 50,000đ)
- **FREE_SHIPPING**: Miễn phí vận chuyển

### Voucher Validation
- Check active status
- Check valid date range
- Check usage limit (global)
- Check per-user limit
- Check minimum order amount
- Redis distributed locking để tránh race condition

## 📊 Caching Strategy

### Redis Cache Keys
```
payment:{paymentId}           - TTL: 15 minutes
voucher:{voucherCode}         - TTL: 1 hour
voucher:lock:{voucherId}      - TTL: 30 seconds
user:vouchers:{userId}        - TTL: 5 minutes
```

## 🔄 Integration với Services khác

### Order Service
- Lấy thông tin order
- Update order status sau payment

### API Gateway
- Route: `/api/payments/**` → payment-service
- Route: `/api/vouchers/**` → payment-service

## 📈 Monitoring

### Actuator Endpoints
```
/actuator/health    - Health check
/actuator/info      - Service info
/actuator/metrics   - Metrics
```

## 🐛 Troubleshooting

### Payment không tạo được
- Check order tồn tại và thuộc về user
- Check payment đã tồn tại cho order chưa
- Check voucher hợp lệ (nếu có)

### Voucher không apply được
- Check voucher code đúng
- Check voucher còn active
- Check trong thời gian valid
- Check đủ điều kiện minimum order amount
- Check chưa vượt quá usage limit

### VNPay callback failed
- Check hash secret đúng
- Check signature validation
- Check payment number tồn tại

## 🔄 Saga Pattern

Payment Service sử dụng **Orchestration-based Saga Pattern** để đảm bảo data consistency trong distributed transactions.

### Saga Features
- ✅ 5-step orchestration (Validate → Reserve → Create → Process → Confirm)
- ✅ Automatic compensation on failure
- ✅ Retry mechanism (max 3 retries)
- ✅ Saga recovery (every 10 minutes)
- ✅ Failed saga retry (every 5 minutes)
- ✅ Old saga cleanup (daily at 2 AM)
- ✅ Complete audit trail
- ✅ Admin monitoring endpoints

### Configuration
```yaml
app:
  payment:
    saga-enabled: true  # Enable/disable saga
  saga:
    timeout-minutes: 30
    recovery-enabled: true
```

### Documentation
- [Saga Pattern Guide](./SAGA_PATTERN_GUIDE.md) - Chi tiết về Saga Pattern
- [Deployment Guide](./DEPLOYMENT.md) - Hướng dẫn deploy với Saga

## 📚 TODO

- [x] ~~Implement Saga Pattern~~ ✅ DONE
- [x] ~~Add saga recovery mechanism~~ ✅ DONE
- [x] ~~Add saga monitoring endpoints~~ ✅ DONE
- [ ] Implement MoMo payment gateway
- [ ] Implement Bank Transfer
- [ ] Add webhook notifications
- [ ] Add payment analytics dashboard
- [ ] Implement voucher recommendation system
- [ ] Add scheduled job to auto-cancel expired payments
- [ ] Add scheduled job to deactivate expired vouchers
- [ ] Add saga metrics and alerting

## 🤝 Contributing

1. Tạo feature branch
2. Commit changes
3. Push to branch
4. Create Pull Request

## 📄 License

MIT License
