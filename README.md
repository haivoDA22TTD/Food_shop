# Order Service

Order Service for Food Shop microservices architecture. Handles order management and shopping cart functionality with Redis caching.

## Features

### Order Management
- ✅ Create orders from shopping cart
- ✅ View order history with pagination
- ✅ Order status tracking
- ✅ Order cancellation (for eligible orders)
- ✅ Admin order management
- ✅ Order statistics and reporting

### Shopping Cart
- ✅ Add/remove items from cart
- ✅ Update item quantities
- ✅ Cart persistence across sessions
- ✅ Redis caching for performance
- ✅ Real-time stock validation
- ✅ Cart summary and totals

### Security & Performance
- ✅ JWT authentication
- ✅ Role-based authorization (USER/ADMIN)
- ✅ Redis caching for cart data
- ✅ Database indexing for performance
- ✅ Input validation and error handling

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL (with JPA/Hibernate)
- **Cache**: Redis (for cart caching)
- **Security**: Spring Security with JWT
- **Service Discovery**: Netflix Eureka
- **Build Tool**: Maven
- **Java Version**: 17

## API Endpoints

### Cart Endpoints (`/api/cart`)
```
GET    /api/cart              # Get user's cart
POST   /api/cart/items        # Add item to cart
PUT    /api/cart/items/{id}   # Update cart item
DELETE /api/cart/items/{id}   # Remove item from cart
DELETE /api/cart              # Clear entire cart
GET    /api/cart/summary      # Get cart summary
```

### Order Endpoints (`/api/orders`)
```
GET    /api/orders            # Get user's orders (paginated)
GET    /api/orders/{id}       # Get specific order
POST   /api/orders            # Create order from cart
PUT    /api/orders/{id}/cancel # Cancel order
GET    /api/orders/{id}/tracking # Get order tracking
```

### Admin Endpoints (`/api/admin/orders`)
```
GET    /api/admin/orders      # Get all orders (with filters)
PUT    /api/admin/orders/{id}/status # Update order status
GET    /api/admin/orders/statistics # Get order statistics
```

## Configuration

### Environment Variables
```bash
# Database
MYSQL_URL=jdbc:mysql://host:port/database
MYSQL_USER=username
MYSQL_PASSWORD=password

# Redis
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=password
REDIS_USERNAME=default
REDIS_SSL_ENABLED=true

# JWT
JWT_SECRET=your-secret-key-must-be-at-least-32-characters-long
JWT_EXPIRATION=86400000

# Service Discovery
EUREKA_URL=http://eureka-server:8761/eureka/

# Product Service
PRODUCT_SERVICE_URL=http://product-service:8082
```

### Application Properties
```yaml
server:
  port: 8083

spring:
  application:
    name: order-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

app:
  cart:
    redis-ttl: 86400      # 24 hours
    max-items: 50         # Maximum items per cart
  order:
    auto-cancel-minutes: 30 # Auto-cancel unpaid orders
```

## Database Schema

### Orders Table
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address TEXT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    product_image VARCHAR(500),
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
);
```

### Carts Table
```sql
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_id (user_id)
);
```

### Cart Items Table
```sql
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id),
    INDEX idx_cart_id (cart_id),
    INDEX idx_product_id (product_id)
);
```

## Redis Caching Strategy

### Cart Caching
- **Key Pattern**: `cart:user:{userId}`
- **TTL**: 24 hours (configurable)
- **Strategy**: Write-through caching
- **Fallback**: Database if Redis unavailable

### Cache Operations
```java
// Cache cart after updates
redisTemplate.opsForValue().set("cart:user:123", cart, Duration.ofSeconds(86400));

// Get cached cart
Cart cachedCart = (Cart) redisTemplate.opsForValue().get("cart:user:123");

// Clear cache on cart operations
redisTemplate.delete("cart:user:123");
```

## Order Status Flow

```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → DELIVERED
    ↓         ↓           ↓
CANCELLED  CANCELLED  CANCELLED
```

### Status Descriptions
- **PENDING**: Order placed, awaiting confirmation
- **CONFIRMED**: Order confirmed, payment processed
- **PREPARING**: Kitchen preparing the order
- **READY_FOR_PICKUP**: Order ready for customer pickup
- **DELIVERED**: Order completed successfully
- **CANCELLED**: Order cancelled (by user or admin)

## Business Rules

### Cart Rules
- Maximum 50 items per cart (configurable)
- Stock validation on add/update operations
- Cart persists across user sessions
- Automatic cache expiration after 24 hours

### Order Rules
- Orders can only be created from non-empty carts
- Product prices are snapshot at order time
- Orders can be cancelled only in PENDING/CONFIRMED status
- Stock validation before order creation
- Automatic cart clearing after successful order

### Admin Rules
- Admins can view all orders with filters
- Admins can update order status with validation
- Status transitions must follow business rules
- Order statistics available with date ranges

## Running the Service

### Local Development
```bash
# Clone repository
git clone <repository-url>
cd order-service

# Set environment variables
export MYSQL_URL=jdbc:mysql://localhost:3306/food_shop_db
export MYSQL_USER=root
export MYSQL_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Run the service
./mvnw spring-boot:run
```

### Docker
```bash
# Build image
docker build -t order-service .

# Run container
docker run -p 8083:8083 \
  -e MYSQL_URL=jdbc:mysql://host:3306/food_shop_db \
  -e REDIS_HOST=redis-host \
  order-service
```

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

## Integration with Other Services

### Product Service
- Validates product availability and stock
- Retrieves product details for cart/order items
- Endpoint: `GET /api/products/{id}`

### Identity Service
- JWT token validation
- User authentication and authorization
- Shared JWT secret for token verification

### API Gateway
- Routes requests to Order Service
- Load balancing and circuit breaker
- CORS configuration for frontend access

## Monitoring and Logging

### Actuator Endpoints
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Logging Configuration
```yaml
logging:
  level:
    com.example.foodshop.order: INFO
    org.springframework.security: DEBUG
    org.springframework.data.redis: INFO
```

## Error Handling

### Common Error Responses
```json
{
  "error": "Product is not available or insufficient stock"
}

{
  "error": "Cart is empty"
}

{
  "error": "Order cannot be cancelled in current status: DELIVERED"
}
```

### HTTP Status Codes
- `200 OK` - Successful operation
- `201 Created` - Order created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Future Enhancements

- [ ] Order payment integration
- [ ] Real-time order status notifications
- [ ] Order history export functionality
- [ ] Advanced order search and filtering
- [ ] Order scheduling and delivery time slots
- [ ] Inventory management integration
- [ ] Order analytics and reporting dashboard