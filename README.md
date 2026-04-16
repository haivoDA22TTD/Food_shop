# 🍔 Food Shop - Hệ thống đặt đồ ăn trực tuyến

## 📋 Mô tả dự án

Food Shop là hệ thống đặt đồ ăn trực tuyến đầy đủ tính năng với:
- **Backend**: Spring Boot REST API
- **Frontend Web**: HTML/CSS/JavaScript với Thymeleaf
- **Desktop App**: Java Swing với FlatLaf
- **Mobile App**: React Native (Expo)
- **AI Chatbot**: Tích hợp Google Gemini 2.5 Flash

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

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL (Aiven Free Tier)
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

2. **Cấu hình database:**
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/foodshop
spring.datasource.username=root
spring.datasource.password=your_password
```

3. **Cấu hình Gemini API:**
```properties
gemini.api.key=your_gemini_api_key
```

4. **Build và chạy:**
```bash
mvn clean install
mvn spring-boot:run
```

Backend chạy tại: `http://localhost:8080`

### Frontend Web

Truy cập: `http://localhost:8080`

### Desktop Application

1. **Build JAR:**
```bash
cd food-shop-swing-client
mvn clean package -DskipTests
```

2. **Chạy:**
```bash
java -jar target/food-shop-swing-client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

3. **Tạo EXE (Windows):**
- Sử dụng file `FoodShop.bat`
- Convert bằng Bat To Exe Converter
- Xem hướng dẫn trong `HUONG_DAN_FILE_BAT_CUOI_CUNG.md`

### Mobile Application

1. **Cài đặt dependencies:**
```bash
cd food-shop-mobile
npm install --legacy-peer-deps
```

2. **Chạy:**
```bash
npx expo start
```

3. **Test trên thiết bị:**
- Cài Expo Go app
- Scan QR code

## 🌐 Deploy

**Environment variables trên Render:**
```
# Database (Aiven MySQL)
DB_HOST=mysql-xxxxx.aivencloud.com
DB_PORT=12345
DB_NAME=defaultdb
DB_USERNAME=avnadmin
DB_PASSWORD=your-mysql-password

# Redis (Aiven Valkey)
REDIS_HOST=redis-xxxxx.aivencloud.com
REDIS_PORT=23456
REDIS_PASSWORD=your-redis-password
REDIS_USERNAME=default
REDIS_SSL=true

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters
JWT_EXPIRATION=86400000

# Admin Account
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
ADMIN_EMAIL=admin@foodshop.com

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
CLOUDINARY_FOLDER=food-shop
CLOUDINARY_ENABLED=true

# Google Gemini AI
GEMINI_API_KEY=your-gemini-api-key

# Application
APP_URL=https://food-shop-iswi.onrender.com
```

Production URL: `https://food-shop-iswi.onrender.com`

### Database & Cache

**MySQL (Aiven Free Tier):**
- 1 GB RAM
- 5 GB Storage
- 25 Max Connections

**Redis/Valkey (Aiven Free Tier):**
- 256 MB RAM
- Used for JWT blacklist & rate limiting

## 👥 Tài khoản mặc định

### Admin
- Username: `admin`
- Password: `admin123`

### Customer
- Username: `customer`
- Password: `customer123`

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

## 🔧 Các tính năng đã cập nhật gần đây

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

### ✅ Desktop App
1. **ChatbotPanel** - Chat với AI trong desktop
2. **CheckoutFrame** - Voucher validation và apply
3. **ApiClient** - Thêm chatbot và voucher endpoints
4. **MainFrameModern** - Thêm AI Trợ lý button
5. **FoodShop.bat** - File BAT để tạo EXE

### ✅ Mobile App
1. Fixed PlatformConstants error
2. Expo SDK 54 compatibility
3. Full navigation setup
4. API integration


## 🐛 Troubleshooting

### Backend không start
- Check MySQL connection
- Check port 8080 có bị chiếm không
- Check environment variables

### Chatbot không hoạt động
- Check Gemini API key
- Check API key hỗ trợ model `gemini-2.5-flash`
- Check Render logs
- Check rate limiting (20 messages/minute)

### Passkey không hoạt động
- Check browser hỗ trợ WebAuthn (Chrome, Edge, Safari)
- Check HTTPS (Passkey chỉ hoạt động trên HTTPS)
- Check RP ID configuration
- Check user verification settings

### Google OAuth2 không hoạt động
- Check Google Console redirect URIs
- Check GOOGLE_CLIENT_ID và GOOGLE_CLIENT_SECRET
- Check JavaScript origins allowed
- Check OAuth2 consent screen

### Redis connection failed
- Check REDIS_SSL=true
- Check REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
- Check Aiven Redis service status
- Check firewall/network

### Rate limit exceeded
- Wait 1 minute và thử lại
- Check Redis connection
- Check rate limit configuration

### Desktop app không chạy
- Check Java 17 đã cài chưa
- Check JAR file path đúng chưa
- Chạy từ command line để xem logs

### Mobile app không kết nối
- Check backend URL trong API service
- Check backend đang chạy
- Check network connection

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

MIT License

## 👨‍💻 Author

Food Shop Team

## 📞 Contact

- Email: support@foodshop.com
- Website: https://food-shop-iswi.onrender.com

---

**Version:** 1.0.0  
**Last Updated:** 16/04/2026  
**Status:** ✅ Production Ready

## 👨‍💻 Author

**haivoDA22TTD (haivoDev)**

- 🎓 Sinh viên Trường Đại học Trà Vinh
- 📧 Email: 110122068@st.tvu.edu.vn
- 🔗 GitHub: [@haivoDA22TTD](https://github.com/haivoDA22TTD)

## � Contact

- Email: 110122068@st.tvu.edu.vn
- Website: https://food-shop-iswi.onrender.com Native + Java Swing)

## � Liên hệ & Hỗ trợ

Nếu bạn có câu hỏi hoặc cần hỗ trợ:

- 📧 Email: 110122068@st.tvu.edu.vn
- 💬 Issues: [GitHub Issues](https://github.com/haivoDA22TTD/food_shop/issues)
- 🐛 Bug Reports: [Report Bug](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=bug)
- ✨ Feature Requests: [Request Feature](https://github.com/haivoDA22TTD/food_shop/issues/new?labels=enhancement)
- 🌐 Website: https://food-shop-iswi.onrender.com

---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star nhé! ⭐**

Made with ❤️ by [haivoDA22TTD](https://github.com/haivoDA22TTD)

</div>

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
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
