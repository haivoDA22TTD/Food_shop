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
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Deployment](#-deployment)
- [API Documentation](#-api-documentation)
- [Screenshots](#-screenshots)
- [Đóng góp](#-đóng-góp)
- [License](#-license)

---

## 🎯 Giới thiệu

**Food Shop** là một ứng dụng web bán hàng thực phẩm trực tuyến được xây dựng với Spring Boot 3, cung cấp trải nghiệm mua sắm mượt mà với giao diện hiện đại và các tính năng quản lý đầy đủ.

### ✨ Điểm nổi bật

- 🔐 **JWT Authentication** - Bảo mật cao với JSON Web Token
- ☁️ **Cloudinary Integration** - Lưu trữ ảnh trên cloud, không lo mất dữ liệu
- 🎨 **Modern UI** - Giao diện đẹp mắt với sky blue gradient theme
- 📱 **Responsive Design** - Tương thích mọi thiết bị
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

### Frontend

| Công nghệ | Mô tả |
|-----------|-------|
| ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1.2-green?logo=thymeleaf) | Template Engine |
| ![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white) | Markup Language |
| ![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white) | Styling |
| ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black) | Client-side Logic |

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

### Environment Variables

#### Development (Local)

Không cần cấu hình, sử dụng giá trị mặc định:

```properties
# Tài khoản admin mặc định
Username: admin
Password: admin123
```

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

**Your Name**

- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com
- LinkedIn: [Your Name](https://linkedin.com/in/yourprofile)

---

## 🙏 Lời cảm ơn

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Cloudinary](https://cloudinary.com)
- [Render](https://render.com)
- [Railway](https://railway.app)
- [Shields.io](https://shields.io) - Badges
- Tất cả contributors đã đóng góp cho dự án

---

## 📞 Liên hệ & Hỗ trợ

Nếu bạn có câu hỏi hoặc cần hỗ trợ:

- 📧 Email: your.email@example.com
- 💬 Issues: [GitHub Issues](https://github.com/yourusername/food-shop/issues)
- 📖 Documentation: [Wiki](https://github.com/yourusername/food-shop/wiki)

---

<div align="center">

**⭐ Nếu bạn thấy dự án hữu ích, hãy cho một star nhé! ⭐**

Made with ❤️ by [Your Name](https://github.com/yourusername)

</div>
