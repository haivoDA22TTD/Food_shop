# 🍔 Food Shop - Hệ thống đặt đồ ăn trực tuyến

## 📋 Mô tả dự án

Food Shop là hệ thống đặt đồ ăn trực tuyến đầy đủ tính năng với kiến trúc Microservices:

- **Architecture**: Microservices với Spring Cloud
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Backend Services**: Spring Boot REST API
- **Frontend Web**: HTML/CSS/JavaScript với Thymeleaf
- **Desktop App**: Java Swing với FlatLaf
- **Mobile App**: React Native (Expo)
- **AI Chatbot**: Tích hợp Google Gemini 2.5 Flash

---

## 🏗️ Kiến Trúc Microservices

```
                    ┌─────────────────┐
                    │   Frontend      │
                    │ React + Vite    │
                    └────────┬────────┘
                             │ HTTPS
                    ┌────────▼────────┐
                    │  API Gateway    │
                    │   Port 8080     │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ Eureka Server   │
                    │   Port 8761     │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┬──────────────┐
        │                    │                    │              │
   ┌────▼─────┐      ┌──────▼──────┐     ┌──────▼──────┐  ┌───▼──────┐
   │ Identity │      │  Product    │     │   Order     │  │ Payment  │
   │ Service  │      │  Service    │     │  Service    │  │ Service  │
   │  8081    │      │   8082      │     │   8083      │  │  8084    │
   └────┬─────┘      └──────┬──────┘     └──────┬──────┘  └────┬─────┘
        │                   │                    │              │
   ┌────▼─────┐      ┌─────▼──────┐      ┌─────▼──────┐  ┌────▼─────┐
   │ MySQL #1 │      │  MySQL #2  │      │  MySQL #3  │  │Postgres#1│
   │  Users   │      │  Products  │      │   Orders   │  │ Vouchers │
   │ Passkeys │      │  Reviews   │      │Order Items │  │  Usage   │
   └──────────┘      └────────────┘      └────────────┘  └──────────┘
```

### 📊 Services Overview

| Service | Port | Database | Status |
|---------|------|----------|--------|
| **Eureka Server** | 8761 | ❌ None | ✅ Deployed |
| **API Gateway** | 8080 | ❌ None | ✅ Deployed |
| **Identity Service** | 8081 | MySQL #1 | ❌ Todo |
| **Product Service** | 8082 | MySQL #2 | ❌ Todo |
| **Order Service** | 8083 | MySQL #3 | ❌ Todo |
| **Payment Service** | 8084 | PostgreSQL #1 | ❌ Todo |

---

## 🚀 Tính năng chính

### 🌐 Web Application
- ✅ Xem danh sách sản phẩm với phân trang
- ✅ Tìm kiếm và lọc sản phẩm theo danh mục
- ✅ Thêm vào giỏ hàng
- ✅ Đặt hàng với nhiều phương thức thanh toán
- ✅ Xem lịch sử đơn hàng
- ✅ Hủy đơn hàng (với giới hạn 3 lần/tháng)
- ✅ Đánh giá sản phẩm (rating + comment)
- ✅ Chat với AI trợ lý thông minh
- ✅ Nhận và áp dụng mã giảm giá
- ✅ Responsive design

### 🖥️ Desktop Application (Java Swing)
- ✅ Giao diện hiện đại với FlatLaf theme
- ✅ Đầy đủ tính năng như web app
- ✅ Chat với AI chatbot
- ✅ Áp dụng voucher trong checkout
- ✅ Xem và quản lý đơn hàng
- ✅ Đánh giá sản phẩm
- ✅ Đóng gói thành file EXE

### 📱 Mobile Application (React Native)
- ✅ Chạy trên Android/iOS với Expo
- ✅ Navigation với React Navigation
- ✅ Đầy đủ tính năng e-commerce
- ✅ UI/UX thân thiện với mobile

### 🤖 AI Chatbot (Google Gemini)
- ✅ Tích hợp Gemini 2.5 Flash API
- ✅ Tự động phân tích intent (voucher, product search, order)
- ✅ Tạo mã giảm giá tự động (WELCOME, COMEBACK, VIP, DEAL)
- ✅ Tư vấn sản phẩm thông minh
- ✅ Fallback logic khi API fail
- ✅ Hoạt động cả khi chưa đăng nhập

### 💰 Hệ thống Voucher
- ✅ Tạo voucher tự động qua chatbot
- ✅ Validate voucher (min order, expiry, usage limit)
- ✅ Áp dụng voucher trong checkout
- ✅ Tracking voucher usage history
- ✅ Hỗ trợ 2 loại: FIXED (giảm cố định) và PERCENTAGE (giảm %)
- ✅ Personal voucher (chỉ user cụ thể dùng được)

### 👤 Quản lý User
- ✅ Đăng ký/Đăng nhập với JWT
- ✅ Đăng nhập với Google OAuth2
- ✅ Đăng nhập với Passkey (WebAuthn)
- ✅ Phân quyền ADMIN/CUSTOMER
- ✅ Tracking số lần hủy đơn
- ✅ Tự động khóa tài khoản nếu hủy đơn > 3 lần/tháng

### 🔐 Bảo mật & Authentication
- ✅ JWT Token Authentication
- ✅ JWT Token Blacklist (Redis)
- ✅ Google OAuth2 Login
- ✅ Passkey/WebAuthn Support
- ✅ Rate Limiting (Bucket4j + Redis)
  - Login: 5 attempts/minute
  - Register: 3 attempts/hour
  - Chatbot: 20 messages/minute
  - Order: 10 orders/hour
  - API: 100 requests/minute

### 📦 Quản lý Đơn hàng
- ✅ Tạo đơn hàng với voucher
- ✅ Tracking trạng thái (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
- ✅ Hủy đơn hàng với validation
- ✅ Lưu thông tin voucher đã dùng (code, discount amount, original amount)

### ⭐ Đánh giá Sản phẩm
- ✅ Rating 1-5 sao
- ✅ Comment
- ✅ Hiển thị user đã đánh giá
- ✅ Eager loading với JOIN FETCH

---

## 🛠️ Tech Stack

### Microservices Infrastructure
- **Service Discovery**: Netflix Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Load Balancing**: Client-side load balancing (Ribbon)
- **Configuration**: Spring Cloud Config (planned)

### Backend Services
- **Framework**: Spring Boot 3.2.0
- **Database**: 
  - MySQL (Aiven Free Tier) - 3 instances
  - PostgreSQL (Aiven Free Tier) - 2 instances
- **Cache**: Redis/Valkey (Aiven Free Tier)
- **Security**: Spring Security + JWT + OAuth2
- **Authentication**: JWT, Google OAuth2, Passkey (WebAuthn)
- **Rate Limiting**: Bucket4j + Redis
- **File Upload**: Cloudinary (với fallback local storage)
- **AI**: Google Gemini 2.5 Flash API
- **Build**: Maven

### Frontend Web
- **Template Engine**: Thymeleaf
- **CSS**: Custom CSS với animations
- **JavaScript**: Vanilla JS
- **Icons**: Emoji + Unicode

### Desktop App
- **Framework**: Java Swing
- **UI Library**: FlatLaf (Modern Look and Feel)
- **HTTP Client**: OkHttp
- **JSON**: Gson
- **Build**: Maven (JAR with dependencies)

### Mobile App
- **Framework**: React Native (Expo SDK 54)
- **Navigation**: React Navigation
- **HTTP Client**: Axios
- **Package Manager**: npm

---

## 🚀 Deployment

### Production URLs

| Service | URL | Status |
|---------|-----|--------|
| **Eureka Server** | https://eureka-server-cqff.onrender.com | ✅ Running |
| **API Gateway** | https://api-gateway-4tdc.onrender.com | ✅ Running |
| **Identity Service** | - | ❌ Not deployed |
| **Product Service** | - | ❌ Not deployed |
| **Order Service** | - | ❌ Not deployed |
| **Payment Service** | - | ❌ Not deployed |

### Environment Variables

#### Eureka Server
```
SPRING_PROFILES_ACTIVE=prod
```

#### API Gateway
```
SPRING_PROFILES_ACTIVE=prod
```

#### Services (Template)
```
SPRING_PROFILES_ACTIVE=prod
MYSQL_URL=jdbc:mysql://...
MYSQL_USER=avnadmin
MYSQL_PASSWORD=...
REDIS_URL=redis://...
REDIS_PASSWORD=...
JWT_SECRET=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
CLOUDINARY_URL=...
GEMINI_API_KEY=...
```

### Database & Cache

**MySQL (Aiven Free Tier):**
- 3 instances (Identity, Product, Order)
- 1 GB RAM per instance
- 5 GB Storage per instance

**PostgreSQL (Aiven Free Tier):**
- 2 instances (Payment, Reserved)
- 1 GB RAM per instance
- 5 GB Storage per instance

**Redis/Valkey (Aiven Free Tier):**
- 256 MB RAM
- Used for JWT blacklist, rate limiting, cache



## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất (blacklist JWT)
- `GET /oauth2/authorization/google` - Đăng nhập Google

### Passkey (WebAuthn)
- `POST /api/passkey/register/options` - Tạo options đăng ký passkey
- `POST /api/passkey/register/verify` - Verify đăng ký passkey
- `POST /api/passkey/login/options` - Tạo options đăng nhập passkey
- `POST /api/passkey/login/verify` - Verify đăng nhập passkey

### Products
- `GET /api/products` - Lấy danh sách sản phẩm
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm

### Orders
- `POST /api/orders/create` - Tạo đơn hàng
- `GET /api/orders/my-orders` - Lấy đơn hàng của user
- `POST /api/orders/{id}/cancel` - Hủy đơn hàng

### Reviews
- `GET /api/reviews/product/{productId}` - Lấy đánh giá của sản phẩm
- `POST /api/reviews/create` - Tạo đánh giá

### Chatbot
- `POST /api/chatbot/chat` - Chat với AI
- `GET /api/chatbot/suggestions` - Lấy gợi ý câu hỏi

### Vouchers
- `GET /api/vouchers/validate?code=...&orderTotal=...` - Validate voucher

---

## 🔧 Các tính năng đã cập nhật gần đây

### ✅ Hotfix Production (April 2026 - Latest)
1. **Frontend Auth Normalization (React + Vite)**  
   - Chuẩn hóa parse payload đăng nhập (`userId`/`id`, fallback `role`, `email`) để tránh lỗi UI khi backend trả thiếu field không quan trọng.
2. **Register UX mượt như Monolith**  
   - Đăng ký thành công có thể set auth session ngay, giảm bước chuyển trang thủ công.
3. **Google OAuth qua API Gateway**  
   - Đồng bộ flow OAuth theo gateway domain, tránh lệch callback giữa frontend/gateway/identity-service.
4. **Gateway Route Update**  
   - Bổ sung route `/oauth2/**` và `/login/oauth2/**` qua `identity-service`.
5. **Gateway CORS Duplicate Header Fix**  
   - Thêm `DedupeResponseHeader` để xử lý lỗi browser chặn do trùng `Access-Control-Allow-Origin`.
6. **Identity Service Eureka Runtime Fix**  
   - Bổ sung explicit dependencies `httpclient5` + `httpcore5` trong `identity-service/pom.xml` để sửa lỗi `ClassNotFoundException: BasicHeaderElement`.
7. **Deploy-Only Workflow Documentation**  
   - Thêm checklist `DEPLOY_CHECKLIST.md` cho quy trình deploy trực tiếp lên host (không chạy local).
8. **Logout + JWT Blacklist Reliability Fix**  
   - Frontend gọi `POST /api/auth/logout` trước khi clear local session để đảm bảo token cũ bị blacklist đúng cách.
   - Backend `TokenBlacklistService` chuyển sang TTL theo thời gian hết hạn thực tế của JWT (thay vì cố định 24h) và thêm xử lý lỗi Redis an toàn hơn.
9. **Updated Order Service**

### ✅ Microservices Architecture (April 2026)
1. **Eureka Server** - Service Discovery & Registry
2. **API Gateway** - Single entry point, routing, load balancing
3. **Service-to-Service Communication** - Feign Client (planned)
4. **Distributed Configuration** - Spring Cloud Config (planned)
5. **Circuit Breaker** - Resilience4j (planned)
6. **Distributed Tracing** - Sleuth + Zipkin (planned)

### ✅ Backend (April 2026)
1. **OAuth2LoginSuccessHandler** - Google OAuth2 integration
2. **PasskeyService** - WebAuthn/Passkey authentication
3. **TokenBlacklistService** - JWT blacklist với Redis
4. **RateLimitService** - Rate limiting với Bucket4j + Redis
5. **RedisConfig** - SSL support cho Aiven Redis/Valkey
6. **VoucherService** - Fixed injection và voucher_usage tracking
7. **ChatbotController** - Tích hợp Gemini 2.5 Flash
8. **OrderController** - Hỗ trợ voucher trong đặt hàng
9. **ReviewController** - API đánh giá sản phẩm
10. **SecurityConfig** - OAuth2 + Passkey support

### ✅ Frontend Web (April 2026)
1. **login.html** - Google OAuth2 button + Passkey button
2. **profile.html** - Passkey management UI
3. **passkey.js** - WebAuthn client implementation
4. **oauth2-redirect.html** - OAuth2 callback handler
5. **chatbot.js** - Chat UI với AI
6. **chatbot.css** - Floating button và chat window
7. **checkout.html** - Voucher input và validation
8. **orders.html** - Fixed JavaScript errors
9. **notifications.js** - Wait for DOM ready

### ✅ Database Migration (April 2026)
1. **Railway → Aiven** - Migrate MySQL từ Railway sang Aiven
2. **Redis/Valkey** - Thêm Redis cho JWT blacklist & rate limiting
3. **SSL Support** - Enable SSL cho Aiven services
4. **Connection Pooling** - Optimize connection pool settings
5. **Multi-Database** - 3 MySQL + 2 PostgreSQL instances

---

## 🐛 Troubleshooting

### Eureka Server
- Check dashboard: https://eureka-server-cqff.onrender.com
- Verify services registered
- Check logs for connection errors

### API Gateway
- Check routing configuration
- Verify Eureka connection
- Test health endpoint: `/actuator/health`
- Nếu browser báo CORS và response có nhiều giá trị `Access-Control-Allow-Origin`, kiểm tra cấu hình dedupe header ở gateway.

### Services
- Check database connections
- Verify Redis connection
- Check environment variables
- Monitor logs for errors
- Nếu `identity-service` log lỗi `ClassNotFoundException: org.apache.hc.core5.http.message.BasicHeaderElement`, kiểm tra lại build mới đã chứa `httpclient5/httpcore5`.
- Với Google OAuth, đảm bảo `APP_URL` của `identity-service` trỏ về domain gateway và redirect URI là `https://api-gateway-4tdc.onrender.com/login/oauth2/code/google`.

---

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

---

## 📄 License

MIT License

**Version:** Microservices Architecture  
**Last Updated:** 25/04/2026  
**Status:** ✅ In Development

---

## 👨‍💻 Author

**haivoDA22TTD (haivoDev)**
- 🎓 Sinh viên Trường Đại học Trà Vinh
- 📧 Email: 110122068@st.tvu.edu.vn
- 🔗 GitHub: [haivoDA22TTD](https://github.com/haivoDA22TTD)

---

## 📞 Liên hệ & Hỗ trợ

Nếu bạn có câu hỏi hoặc cần hỗ trợ:
- 📧 Email: 110122068@st.tvu.edu.vn
- 💬 Issues: [GitHub Issues](https://github.com/haivoDA22TTD/food_shop/issues)
- 🐛 Bug Reports: [Report Bug](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=bug)
- ✨ Feature Requests: [Request Feature](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=enhancement)

---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star nhé! ⭐**

Made with ❤️ by [haivoDA22TTD](https://github.com/haivoDA22TTD)

</div>

---

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) - Microservices Framework
- [Netflix Eureka](https://github.com/Netflix/eureka) - Service Discovery
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) - API Gateway
- [Spring Security OAuth2](https://spring.io/projects/spring-security-oauth) - OAuth2 Client
- [FlatLaf](https://www.formdev.com/flatlaf/) - Modern Look and Feel cho Java Swing
- [Cloudinary](https://cloudinary.com) - Cloud Image Storage
- [Render](https://render.com) - Backend Hosting
- [Aiven](https://aiven.io) - MySQL & Redis/Valkey Hosting
- [Google Gemini](https://ai.google.dev/) - AI Chatbot
- [Google OAuth2](https://developers.google.com/identity/protocols/oauth2) - Authentication
- [WebAuthn](https://webauthn.io/) - Passkey Standard
- [Bucket4j](https://bucket4j.com/) - Rate Limiting
- [OkHttp](https://square.github.io/okhttp/) - HTTP Client
- [Gson](https://github.com/google/gson) - JSON Parser
- [Expo](https://expo.dev/) - React Native Framework
- Tất cả contributors đã đóng góp cho dự án
