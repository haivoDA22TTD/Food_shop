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
- ✅ Phân quyền ADMIN/CUSTOMER
- ✅ Tracking số lần hủy đơn
- ✅ Tự động khóa tài khoản nếu hủy đơn > 3 lần/tháng

### 📦 Quản lý Đơn hàng
- ✅ Tạo đơn hàng với voucher
- ✅ Tracking trạng thái (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
- ✅ Hủy đơn hàng với validation
- ✅ Lưu thông tin voucher đã dùng (code, discount amount, original amount)

### ⭐ Đánh giá Sản phẩm
- ✅ Rating 1-5 sao
- ✅ Comment
- ✅ Hiển thị user đã đánh giá


## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL (Railway)
- **Security**: Spring Security + JWT
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
SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
GEMINI_API_KEY=...
```


### Database (Railway)

MySQL database hosted trên Railway.

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập

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

### ✅ Backend
1. **VoucherService** - Fixed injection và voucher_usage tracking
2. **ChatbotController** - Tích hợp Gemini 2.5 Flash
3. **OrderController** - Hỗ trợ voucher trong đặt hàng
4. **ReviewController** - API đánh giá sản phẩm
5. **SecurityConfig** - Permit chatbot endpoint

### ✅ Frontend Web
1. **chatbot.js** - Chat UI với AI
2. **chatbot.css** - Floating button và chat window
3. **checkout.html** - Voucher input và validation
4. **orders.html** - Fixed JavaScript errors
5. **notifications.js** - Wait for DOM ready

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

**Version:** 1.0.0  
**Last Updated:** 05/04/2026  
**Status:** ✅ Production Ready

## 👨‍💻 Author

**haivoDA22TTD (haivoDev)**

- 🎓 Sinh viên Trường Đại học Trà Vinh
- 📧 Email: 110122068@st.tvu.edu.vn
- 🔗 GitHub: [haivoDA22TTD](https://github.com/haivoDA22TTD)



---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star nhé! ⭐**

Made with ❤️ by [haivoDA22TTD](https://github.com/haivoDA22TTD)

</div>

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [FlatLaf](https://www.formdev.com/flatlaf/) - Modern Look and Feel cho Java Swing
- [Cloudinary](https://cloudinary.com) - Cloud Image Storage
- [Render](https://render.com) - Backend Hosting
- [Railway](https://railway.app) - Database Hosting
- [Google Gemini](https://ai.google.dev/) - AI Chatbot
- [OkHttp](https://square.github.io/okhttp/) - HTTP Client
- [Gson](https://github.com/google/gson) - JSON Parser
- [Expo](https://expo.dev/) - React Native Framework
- Tất cả contributors đã đóng góp cho dự án
