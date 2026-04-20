# Identity Service - Authentication & User Management

## 📋 Vai Trò

Quản lý người dùng, xác thực, phân quyền

## 🎯 Chức Năng

- Username/Password Login
- OAuth2 Google Login
- Passkey Registration & Login (WebAuthn)
- JWT Token Management
- Token Blacklist (Redis)
- User Profile Management
- Role-Based Access Control (USER, ADMIN)

## 🔧 Công Nghệ

- **Spring Boot 3.2.0** - Framework
- **Spring Security** - Authentication & Authorization
- **JWT** - Token-based auth
- **OAuth2 Client** - Google login
- **WebAuthn** - Passkey support
- **MySQL** - Database (Aiven Free Tier)
- **Redis** - Token blacklist (Aiven Valkey)
- **Eureka Client** - Service discovery

## 📊 Database: MySQL #1 (Aiven)

```sql
users
├── id (PK)
├── username (unique)
├── email (unique)
├── password_hash
├── role (USER, ADMIN)
├── google_id (nullable)
├── account_locked
├── created_at
└── updated_at

passkey_credentials
├── id (PK)
├── user_id (FK → users)
├── credential_id (unique)
├── public_key (text)
├── nickname
├── sign_count
├── is_active
├── created_at
└── last_used_at

passkey_challenges
├── id (PK)
├── challenge (unique)
├── user_id (FK → users, nullable)
├── username
├── type (registration, authentication)
├── used
├── expires_at
└── created_at
```

## ⚠️ Connection Pool - Aiven MySQL Free Tier

**Aiven Limits:**
- Max connections: 25 total
- Identity Service: **Max 3 connections**

**HikariCP Config:**
```yaml
hikari:
  maximum-pool-size: 3      # KHÔNG TĂNG!
  minimum-idle: 1
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
  leak-detection-threshold: 60000
```

## 🌐 Environment Variables

```
SPRING_PROFILES_ACTIVE=prod

# MySQL (Aiven)
MYSQL_URL=jdbc:mysql://mysql-identity.aivencloud.com:12345/identity_db?useSSL=true
MYSQL_USER=avnadmin
MYSQL_PASSWORD=your_password

# Redis (Aiven Valkey)
REDIS_HOST=redis.aivencloud.com
REDIS_PORT=12345
REDIS_PASSWORD=your_redis_password
REDIS_USERNAME=default
REDIS_SSL=true

# JWT
JWT_SECRET=your_jwt_secret_min_32_chars
JWT_EXPIRATION=86400000

# OAuth2 Google
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# App URL
APP_URL=https://api-gateway-4tdc.onrender.com
```

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất
- `GET /api/auth/google` - OAuth2 Google

### Passkey
- `POST /api/passkey/register/options` - Tạo options đăng ký
- `POST /api/passkey/register/verify` - Verify đăng ký
- `POST /api/passkey/login/options` - Tạo options đăng nhập
- `POST /api/passkey/login/verify` - Verify đăng nhập

### User
- `GET /api/users/profile` - Xem profile
- `PUT /api/users/profile` - Sửa profile
- `GET /api/users/{id}` - Lấy user (ADMIN)

## 🚀 Run Local

```bash
mvn spring-boot:run
```

Access: http://localhost:8081

## 🐳 Docker

```bash
docker build -t identity-service:latest .
docker run -d -p 8081:8081 identity-service:latest
```

## ☁️ Deploy to Render

**Environment:** Docker  
**Root Directory:** identity-service  
**Health Check:** /actuator/health

## 📊 Monitoring

- **Health:** http://localhost:8081/actuator/health
- **Metrics:** http://localhost:8081/actuator/metrics
- **Eureka:** Check registration on Eureka Dashboard

## 📝 Files

- `STRUCTURE.md` - Cấu trúc code chi tiết
- `DEPLOY.md` - Hướng dẫn deploy lên Render
- `ENV_TEMPLATE.md` - Template environment variables (root directory)

## ✅ Status

✅ Code hoàn tất - Sẵn sàng deploy
