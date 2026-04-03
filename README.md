# 🍔 Food Shop - Spring Boot E-commerce Application

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Ứng dụng bán hàng thực phẩm trực tuyến với Spring Boot, JWT Authentication, và Cloudinary**

[Demo Live](https://food-shop-iswi.onrender.com) • [Báo cáo lỗi](https://github.com/yourusername/food-shop/issues) • [Yêu cầu tính năng](https://github.com/yourusername/food-shop/issues)

</div>

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Desktop Application](#-desktop-application)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Deployment](#-deployment)
- [API Documentation](#-api-documentation)
- [Screenshots](#-screenshots)
- [Đóng góp](#-đóng-góp)
- [License](#-license)

---

## 🎯 Giới thiệu

**Food Shop** là một hệ thống bán hàng thực phẩm hoàn chỉnh với 3 nền tảng:
- 🌐 **Web Application** - Giao diện web hiện đại với Thymeleaf
- 💻 **Desktop Application** - Ứng dụng Java Swing với FlatLaf Look and Feel
- 🔌 **REST API Backend** - Spring Boot với JWT Authentication

### ✨ Điểm nổi bật

- 🔐 **JWT Authentication** - Bảo mật cao với JSON Web Token
- ☁️ **Cloudinary Integration** - Lưu trữ ảnh trên cloud, không lo mất dữ liệu
- 🎨 **Modern UI** - Giao diện đẹp mắt với sky blue gradient theme
- 📱 **Responsive Design** - Web tương thích mọi thiết bị
- 🖥️ **Desktop App** - Ứng dụng standalone không cần cài Java
- 🐳 **Docker Ready** - Deploy dễ dàng với Docker
- 🚀 **Production Ready** - Đã deploy trên Render + Railway

---

## 🚀 Tính năng

### 👤 Khách hàng

- ✅ Đăng ký/Đăng nhập với JWT
- 🛒 Giỏ hàng với localStorage
- 🔍 Tìm kiếm và lọc sản phẩm theo danh mục
- 💳 Thanh toán với nhiều phương thức (COD, Bank Transfer, MoMo, Credit Card)
- 📦 Theo dõi đơn hàng theo thời gian thực
- ⭐ Đánh giá sản phẩm sau khi nhận hàng
- 🔔 Thông báo toast hiện đại

### 👨‍💼 Admin

- 📊 Dashboard với thống kê tổng quan
- 📦 Quản lý sản phẩm (CRUD)
- 🖼️ Upload ảnh với drag-and-drop
- 📋 Quản lý đơn hàng và cập nhật trạng thái
- 👥 Quản lý người dùng
- 📈 Báo cáo doanh thu và tồn kho

---

## 🛠️ Công nghệ sử dụng

### Backend

| Công nghệ | Phiên bản | Mô tả |
|-----------|-----------|-------|
| ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?logo=spring-boot) | 3.2.0 | Framework chính |
| ![Java](https://img.shields.io/badge/Java-17-orange?logo=java) | 17 | Ngôn ngữ lập trình |
| ![Spring Security](https://img.shields.io/badge/Spring%20Security-6.1.1-green?logo=spring-security) | 6.1.1 | Bảo mật & Authentication |
| ![JWT](https://img.shields.io/badge/JWT-0.12.3-black?logo=json-web-tokens) | 0.12.3 | Token-based authentication |
| ![Hibernate](https://img.shields.io/badge/Hibernate-6.3.1-59666C?logo=hibernate) | 6.3.1 | ORM Framework |
| ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql) | 8.0 | Database |

### Web Frontend

| Công nghệ | Mô tả |
|-----------|-------|
| ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1.2-green?logo=thymeleaf) | Template Engine |
| ![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white) | Markup Language |
| ![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white) | Styling |
| ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black) | Client-side Logic |

### Desktop Application

| Công nghệ | Phiên bản | Mô tả |
|-----------|-----------|-------|
| ![Java Swing](https://img.shields.io/badge/Java%20Swing-17-orange?logo=java) | 17 | GUI Framework |
| ![FlatLaf](https://img.shields.io/badge/FlatLaf-3.2.5-blue) | 3.2.5 | Modern Look and Feel |
| ![OkHttp](https://img.shields.io/badge/OkHttp-4.12.0-green) | 4.12.0 | HTTP Client |
| ![Gson](https://img.shields.io/badge/Gson-2.10.1-orange) | 2.10.1 | JSON Parser |
| ![Maven](https://img.shields.io/badge/Maven-3.9.5-red?logo=apache-maven) | 3.9.5 | Build Tool |

### Cloud & DevOps

| Công nghệ | Mô tả |
|-----------|-------|
| ![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?logo=cloudinary&logoColor=white) | Image Storage |
| ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) | Containerization |
| ![Render](https://img.shields.io/badge/Render-46E3B7?logo=render&logoColor=white) | Backend Hosting |
| ![Railway](https://img.shields.io/badge/Railway-0B0D0E?logo=railway&logoColor=white) | Database Hosting |

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                    Desktop Application                       │
│         (Java Swing + FlatLaf + OkHttp + Gson)              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ REST API (JWT)
                     │
┌────────────────────▼────────────────────────────────────────┐
│                         Frontend                             │
│  (Thymeleaf + HTML/CSS/JS + Toast Notifications)           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP/HTTPS
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    Spring Boot Backend                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Controllers  │  │   Services   │  │ Repositories │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │              │
│  ┌──────▼──────────────────▼──────────────────▼───────┐    │
│  │          Spring Security + JWT Filter              │    │
│  └────────────────────────────────────────────────────┘    │
└────────────────────┬────────────────────┬───────────────────┘
                     │                    │
        ┌────────────▼──────────┐  ┌─────▼──────────┐
        │   MySQL Database      │  │   Cloudinary   │
        │   (Railway)           │  │  (Image CDN)   │
        └───────────────────────┘  └────────────────┘
```

---

## 💻 Desktop Application

### 📥 Tải về & Cài đặt

#### Cho người dùng cuối:

1. **Tải file nén** `FoodShopApp.zip`
2. **Giải nén** vào thư mục bất kỳ (ví dụ: `D:\FoodShopApp\`)
3. **Double-click** file `FoodShop.exe` để chạy
4. **Không cần cài đặt Java** - JRE đã được bundle sẵn!

#### Cấu trúc thư mục:
```
FoodShopApp/
├── FoodShop.exe              # File thực thi
├── app/
│   └── food-shop-swing-client-1.0.0.jar
└── jre/                      # Java Runtime Environment
    └── bin/
        └── javaw.exe
```

### 🔨 Build từ source code

#### Yêu cầu:
- Java 17 JDK
- Maven 3.6+
- IntelliJ IDEA (khuyến nghị)

#### Các bước build:

1. **Clone repository:**
```bash
git clone https://github.com/haivoDA22TTD/food_shop.git
cd food_shop/food-shop-swing-client
```

2. **Build với Maven:**
```bash
mvn clean package
```

3. **File JAR được tạo tại:**
```
target/food-shop-swing-client-1.0.0.jar
```

4. **Chạy JAR:**
```bash
java -jar target/food-shop-swing-client-1.0.0.jar
```

### 📦 Đóng gói thành EXE

#### Chuẩn bị:

1. **Tạo cấu trúc thư mục:**
```
D:\FoodShopApp\
├── app\
│   └── food-shop-swing-client-1.0.0.jar
└── jre\
    └── (copy JDK 17 vào đây)
```

2. **Copy JRE:**
- Copy toàn bộ thư mục JDK 17 vào `jre\`
- Hoặc download JRE 17 từ [Adoptium](https://adoptium.net/)

3. **Tạo file launcher:**
- File `FoodShop.bat` đã có sẵn trong project
- Copy vào thư mục `D:\FoodShopApp\`

4. **Convert BAT sang EXE:**
- Download [Bat To Exe Converter](http://www.f2ko.de/en/b2e.php) (Free)
- Load file `FoodShop.bat`
- Convert thành `FoodShop.exe`

5. **Phân phối:**
- Nén toàn bộ thư mục `FoodShopApp\`
- Gửi file ZIP cho users
- Users chỉ cần giải nén và chạy `FoodShop.exe`

📖 **Chi tiết**: Xem file `food-shop-swing-client/BUILD_EXE_GUIDE.md`

### 🎨 Giao diện Desktop App

#### Màn hình chính:
- Grid layout hiển thị sản phẩm với ảnh từ Cloudinary
- Hover effects với animations
- Search và filter theo category
- Gradient background (sky blue theme)

#### Tính năng:
- **Login/Register** - Form đăng nhập/đăng ký
- **Product Grid** - Hiển thị sản phẩm với card design
- **Product Detail** - Chi tiết sản phẩm + reviews
- **Cart** - Giỏ hàng với tính tổng tiền
- **Checkout** - Form thanh toán
- **Orders** - Danh sách đơn hàng với status colors
- **Reviews** - Dialog đánh giá với star rating

### 🔧 Cấu hình Backend URL

Mặc định app kết nối đến production backend:
```java
private static final String BASE_URL = "https://food-shop-iswi.onrender.com";
```

Để kết nối local backend, sửa trong `ApiClient.java`:
```java
private static final String BASE_URL = "http://localhost:8080";
```

---

## 💻 Cài đặt

### Yêu cầu hệ thống

- ☕ Java 17 hoặc cao hơn
- 🗄️ MySQL 8.0 hoặc cao hơn
- 📦 Maven 3.6+
- 🐳 Docker (tùy chọn)

### Clone Repository

```bash
git clone https://github.com/yourusername/food-shop.git
cd food-shop
```

### Cài đặt Dependencies

```bash
mvn clean install
```

### Cấu hình Database

```sql
CREATE DATABASE food_shop;
```

### Chạy ứng dụng

#### Cách 1: Maven

```bash
mvn spring-boot:run
```

#### Cách 2: JAR file

```bash
mvn clean package
java -jar target/food-shop-1.0.0.jar
```

#### Cách 3: Docker

```bash
docker build -t food-shop .
docker run -p 8080:8080 food-shop
```

Truy cập: `http://localhost:8080`

---

## ⚙️ Cấu hình

### Spring Profiles

Dự án sử dụng 2 profiles:

- **dev** - Local development (mặc định)
- **prod** - Production deployment



#### Production (Render/Railway)

Cần set các biến môi trường sau:

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://host:port/database
DB_USERNAME=root
DB_PASSWORD=your-password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Admin Account
ADMIN_USERNAME=your-admin-username
ADMIN_PASSWORD=your-secure-password
ADMIN_EMAIL=admin@yourdomain.com

# Cloudinary
CLOUDINARY_ENABLED=true
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
CLOUDINARY_FOLDER=food-shop
```

📖 **Chi tiết**: Xem file `SPRING_PROFILES_GUIDE.md`

---

## 🚀 Deployment

### Deploy lên Render + Railway

#### Bước 1: Setup Database trên Railway

1. Tạo MySQL database trên [Railway](https://railway.app)
2. Copy Public URL

#### Bước 2: Deploy Backend trên Render

1. Tạo Web Service trên [Render](https://render.com)
2. Connect GitHub repository
3. Set Environment Variables (xem file `RENDER_COPY_PASTE.txt`)
4. Deploy

#### Bước 3: Cấu hình Cloudinary

1. Tạo tài khoản [Cloudinary](https://cloudinary.com)
2. Copy API credentials
3. Set environment variables

📖 **Hướng dẫn chi tiết**: 
- `DEPLOYMENT.md` - Deploy đầy đủ
- `CLOUDINARY_SETUP.md` - Cấu hình Cloudinary
- `DOCKER_DEPLOY.md` - Deploy với Docker

---

## 📚 API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Đăng nhập và nhận JWT token |

### Products

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/products` | Lấy danh sách sản phẩm | Public |
| GET | `/api/products/{id}` | Lấy chi tiết sản phẩm | Public |
| POST | `/admin/products/save` | Tạo/cập nhật sản phẩm | Admin |
| DELETE | `/admin/products/delete/{id}` | Xóa sản phẩm | Admin |

### Orders

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/orders/create` | Tạo đơn hàng mới | Customer |
| GET | `/api/orders/my-orders` | Lấy đơn hàng của user | Customer |
| GET | `/admin/orders` | Lấy tất cả đơn hàng | Admin |
| POST | `/admin/orders/{id}/status` | Cập nhật trạng thái | Admin |

### Reviews

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/reviews/create` | Tạo đánh giá | Customer |
| GET | `/api/reviews/product/{id}` | Lấy đánh giá sản phẩm | Public |

---

## 📸 Screenshots

### Trang chủ
![Homepage](docs/screenshots/homepage.png)

### Giỏ hàng
![Cart](docs/screenshots/cart.png)

### Admin Dashboard
![Admin Dashboard](docs/screenshots/admin-dashboard.png)

### Quản lý sản phẩm
![Product Management](docs/screenshots/product-management.png)

---

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Vui lòng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

## 📝 License

Dự án này được phân phối dưới giấy phép MIT. Xem file `LICENSE` để biết thêm chi tiết.

---

## 👨‍💻 Tác giả

**haivoDA22TTD (haivoDev)**

- 👤 GitHub: [@haivoDA22TTD](https://github.com/haivoDA22TTD)
- 📧 Email: 110122068@st.tvu.edu.vn
- 🎓 Sinh viên Trường Đại học Trà Vinh
- 💼 Full Stack Developer (Spring Boot + Java Swing + Web)

---

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [FlatLaf](https://www.formdev.com/flatlaf/) - Modern Look and Feel cho Java Swing
- [Cloudinary](https://cloudinary.com) - Cloud Image Storage
- [Render](https://render.com) - Backend Hosting
- [Railway](https://railway.app) - Database Hosting
- [OkHttp](https://square.github.io/okhttp/) - HTTP Client
- [Gson](https://github.com/google/gson) - JSON Parser
- [Shields.io](https://shields.io) - Badges
- [Bat To Exe Converter](http://www.f2ko.de/en/b2e.php) - BAT to EXE Tool
- Tất cả contributors đã đóng góp cho dự án

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
