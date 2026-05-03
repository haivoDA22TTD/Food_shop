# 🍔 Food Shop - Hệ thống đặt đồ ăn trực tuyến

## 📋 Mô tả dự án

Food Shop là hệ thống đặt đồ ăn trực tuyến với kiến trúc Microservices hiện đại:

- **Architecture**: Microservices với Spring Cloud
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Backend Services**: Spring Boot REST API
- **Frontend**: React + Vite + TypeScript
- **AI Chatbot**: Tích hợp Google Gemini (planned)

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
┌────────┼────────────────┬──────────────┐
│        │                │              │
┌───▼────┐  ┌──────▼──────┐  ┌──────▼──────┐  ┌───▼──────┐
│Identity│  │  Product    │  │   Order     │  │ Payment  │
│Service │  │  Service    │  │  Service    │  │ Service  │
│  8081  │  │   8082      │  │   8083      │  │  8084    │
└───┬────┘  └──────┬──────┘  └──────┬──────┘  └────┬─────┘
    │              │                │              │
┌───▼────┐  ┌─────▼──────┐  ┌─────▼──────┐  ┌────▼─────┐
│MySQL #1│  │  MySQL #2  │  │  MySQL #3  │  │Postgres#1│
│ Users  │  │  Products  │  │   Orders   │  │ Payments │
│Passkeys│  │  Reviews   │  │Order Items │  │ Vouchers │
└────────┘  └────────────┘  └────────────┘  └──────────┘
```

### 📊 Services Overview

| Service | Port | Database | Status |
|---------|------|----------|--------|
| **Eureka Server** | 8761 | ❌ None | ✅ Deployed |
| **API Gateway** | 8080 | ❌ None | ✅ Deployed |
| **Identity Service** | 8081 | MySQL #1 | ✅ Deployed |
| **Product Service** | 8082 | MySQL #2 | ✅ Deployed |
| **Order Service** | 8083 | MySQL #3 | ✅ Deployed |
| **Payment Service** | 8084 | PostgreSQL #1 | ⏳ In Progress |

---

## 🚀 Tính năng chính

### 🔐 Authentication & Authorization
- ✅ JWT Token Authentication
- ✅ Google OAuth2 Login
- ✅ Passkey/WebAuthn Support
- ✅ JWT Token Blacklist (Redis)
- ✅ Role-based Access Control (ADMIN/CUSTOMER)

### 🛍️ Product Management
- ✅ Xem danh sách sản phẩm với phân trang
- ✅ Tìm kiếm và lọc sản phẩm
- ✅ Upload ảnh sản phẩm (Cloudinary)
- ✅ Quản lý danh mục sản phẩm
- ✅ Admin: CRUD sản phẩm

### 🛒 Shopping & Orders
- ✅ Giỏ hàng (Cart)
- ✅ Đặt hàng với nhiều sản phẩm
- ✅ Áp dụng mã giảm giá (Voucher)
- ✅ Tracking trạng thái đơn hàng
- ✅ Hủy đơn hàng (với giới hạn)
- ✅ Lịch sử đơn hàng

### ⭐ Reviews & Ratings
- ✅ Đánh giá sản phẩm (1-5 sao)
- ✅ Comment sản phẩm
- ✅ Hiển thị đánh giá theo sản phẩm

### 🤖 AI Chatbot (Planned)
- ⏳ Tích hợp Google Gemini API
- ⏳ Tư vấn sản phẩm thông minh
- ⏳ Tạo voucher tự động

### 🔒 Security Features
- ✅ Rate Limiting (Redis-based)
- ✅ CORS Configuration
- ✅ JWT Blacklist on Logout
- ✅ Password Encryption (BCrypt)
- ✅ SSL/TLS Support

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Microservices**: Spring Cloud 2023.0.0
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security + JWT + OAuth2
- **Database**: MySQL (Aiven)
- **Cache**: Redis/Valkey (Aiven)
- **File Upload**: Cloudinary
- **Build**: Maven

### Frontend
- **Framework**: React 18 + Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **State Management**: Zustand
- **Routing**: React Router v6

### Infrastructure
- **Hosting**: Render.com
- **Database**: Aiven (MySQL + Redis)
- **CDN**: Cloudinary
- **CI/CD**: GitHub Actions

---

## 🚀 Deployment

### Production Deployment

| Service | Status |
|---------|--------|
| **Frontend** | ✅ Running |
| **API Gateway** | ✅ Running |
| **Eureka Server** | ✅ Running |
| **Identity Service** | ✅ Running |
| **Product Service** | ✅ Running |
| **Order Service** | ✅ Running |

### Environment Variables

#### API Gateway
```env
SPRING_PROFILES_ACTIVE=prod
FRONTEND_URL=<frontend-url>
IDENTITY_SERVICE_URL=<identity-service-url>
PRODUCT_SERVICE_URL=<product-service-url>
ORDER_SERVICE_URL=<order-service-url>
```

#### Identity Service
```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<mysql-host>
MYSQL_PORT=<mysql-port>
MYSQL_DATABASE=identity_db
MYSQL_USER=<mysql-user>
MYSQL_PASSWORD=<mysql-password>
REDIS_HOST=<redis-host>
REDIS_PORT=<redis-port>
REDIS_PASSWORD=<redis-password>
REDIS_SSL=true
JWT_SECRET=<jwt-secret>
GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>
APP_URL=<api-gateway-url>
```

#### Product Service
```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<mysql-host>
MYSQL_PORT=<mysql-port>
MYSQL_DATABASE=product_db
MYSQL_USER=<mysql-user>
MYSQL_PASSWORD=<mysql-password>
JWT_SECRET=<jwt-secret> (same as Identity Service)
CLOUDINARY_ENABLED=true
CLOUDINARY_CLOUD_NAME=<cloudinary-cloud-name>
CLOUDINARY_API_KEY=<cloudinary-api-key>
CLOUDINARY_API_SECRET=<cloudinary-api-secret>
CLOUDINARY_FOLDER=food-shop
```

#### Order Service
```env
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<mysql-host>
MYSQL_PORT=<mysql-port>
MYSQL_DATABASE=order_db
MYSQL_USER=<mysql-user>
MYSQL_PASSWORD=<mysql-password>
JWT_SECRET=<jwt-secret> (same as Identity Service)
IDENTITY_SERVICE_URL=<identity-service-url>
PRODUCT_SERVICE_URL=<product-service-url>
```

---

## 📝 API Endpoints

### Authentication (`/api/auth`)
- `POST /register` - Đăng ký tài khoản
- `POST /login` - Đăng nhập
- `POST /logout` - Đăng xuất
- `GET /oauth2/authorization/google` - Google OAuth2

### Passkey (`/api/passkey`)
- `POST /register/options` - Tạo options đăng ký passkey
- `POST /register/verify` - Verify đăng ký passkey
- `POST /login/options` - Tạo options đăng nhập passkey
- `POST /login/verify` - Verify đăng nhập passkey

### Products (`/api/products`)
- `GET /` - Lấy danh sách sản phẩm
- `GET /{id}` - Chi tiết sản phẩm
- `POST /admin/products` - Tạo sản phẩm (Admin)
- `PUT /admin/products/{id}` - Cập nhật sản phẩm (Admin)
- `DELETE /admin/products/{id}` - Xóa sản phẩm (Admin)

### Orders (`/api/orders`)
- `POST /create` - Tạo đơn hàng
- `GET /my-orders` - Lấy đơn hàng của user
- `GET /{id}` - Chi tiết đơn hàng
- `POST /{id}/cancel` - Hủy đơn hàng
- `PUT /admin/orders/{id}/status` - Cập nhật trạng thái (Admin)

### Reviews (`/api/reviews`)
- `GET /product/{productId}` - Lấy đánh giá của sản phẩm
- `POST /create` - Tạo đánh giá

### Chatbot (`/api/chatbot`)
- `POST /chat` - Chat với AI
- `GET /suggestions` - Gợi ý câu hỏi

---

## 🔧 Recent Updates

### ✅ Latest (May 2026)

1. **Order Service Integration**
   - Tích hợp Order Service vào hệ thống
   - API tạo đơn hàng, xem lịch sử, hủy đơn
   - Tracking trạng thái đơn hàng
   - Validation voucher và stock

2. **CORS Fix**
   - Sửa lỗi CORS với `allowedOriginPatterns`
   - Cho phép tất cả subdomain `*.onrender.com`
   - Deduplicate CORS headers

3. **Admin Product Management**
   - Thêm route `/api/admin/products/**` vào Gateway
   - Upload ảnh sản phẩm với Cloudinary
   - CRUD sản phẩm đầy đủ

4. **CI/CD Workflow**
   - GitHub Actions cho monorepo
   - Auto-detect changed services
   - Deploy hooks cho Render

---

## 🐛 Troubleshooting

### CORS Errors
- Kiểm tra `FRONTEND_URL` trong API Gateway
- Verify CORS config cho phép origin của frontend
- Check browser console cho chi tiết lỗi

### Service Discovery Issues
- Kiểm tra Eureka Dashboard
- Verify services đã register thành công
- Check `RENDER_EXTERNAL_HOSTNAME` trong env

### Database Connection
- Verify MySQL credentials trong env
- Check connection pool settings
- Monitor logs cho connection errors

### JWT Token Issues
- Đảm bảo `JWT_SECRET` giống nhau ở tất cả services
- Check token expiration time
- Verify Redis connection cho blacklist

---

## 🤝 Contributing

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

MIT License

---

## 👨‍💻 Author

**haivoDA22TTD (haivoDev)**
- 🎓 Sinh viên Đại học Trà Vinh
- 📧 Email: 110122068@st.tvu.edu.vn
- 🔗 GitHub: [haivoDA22TTD](https://github.com/haivoDA22TTD)

---

## 📞 Liên hệ & Hỗ trợ

- 📧 Email: 110122068@st.tvu.edu.vn
- 💬 Issues: [GitHub Issues](https://github.com/haivoDA22TTD/food_shop/issues)
- 🐛 Bug Reports: [Report Bug](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=bug)
- ✨ Feature Requests: [Request Feature](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=enhancement)

---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star nhé! ⭐**

Made with ❤️ by [haivoDA22TTD](https://github.com/haivoDA22TTD)

**Version:** 1.0.0 | **Last Updated:** 03/05/2026 | **Status:** ✅ In Production

</div>

---

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) - Microservices Framework
- [Netflix Eureka](https://github.com/Netflix/eureka) - Service Discovery
- [React](https://react.dev/) - Frontend Framework
- [Vite](https://vitejs.dev/) - Build Tool
- [Tailwind CSS](https://tailwindcss.com/) - CSS Framework
- [Cloudinary](https://cloudinary.com) - Image Storage
- [Render](https://render.com) - Hosting Platform
- [Aiven](https://aiven.io) - Database & Cache Hosting
