# 📱 Food Shop - Mobile App (React Native + Expo)

## 🎯 Giới thiệu

Ứng dụng mobile Food Shop được xây dựng bằng **React Native + Expo**, kết nối với backend Spring Boot qua REST API.

## ✨ Tính năng

### Khách hàng
- 🔐 Đăng nhập/Đăng ký với JWT
- 🛍️ Xem danh sách sản phẩm
- 🔍 Tìm kiếm và lọc sản phẩm
- 🛒 Giỏ hàng
- 💳 Thanh toán
- 📦 Theo dõi đơn hàng
- ❌ Hủy đơn hàng
- ⭐ Đánh giá sản phẩm

## 🛠️ Công nghệ

- **React Native** - Framework
- **Expo** - Development Platform
- **JavaScript/TypeScript** - Ngôn ngữ
- **Axios** - HTTP Client
- **AsyncStorage** - Local Storage
- **React Navigation** - Navigation

## 📋 Yêu cầu

- Node.js >= 18
- npm hoặc yarn
- Expo Go app trên điện thoại (Android/iOS)

## 🚀 Cài đặt & Chạy

### 1. Cài dependencies:
```bash
npm install
```

### 2. Chạy app:
```bash
npm start
```

### 3. Chạy trên thiết bị:

#### 📱 Điện thoại thật (Khuyến nghị):
1. Cài **Expo Go** app từ:
   - Android: [Google Play](https://play.google.com/store/apps/details?id=host.exp.exponent)
   - iOS: [App Store](https://apps.apple.com/app/expo-go/id982107779)
2. Quét QR code hiện trên terminal
3. App sẽ chạy ngay trên điện thoại!

#### 🌐 Web Browser:
```bash
npm run web
```

#### 📱 Android Emulator (nếu có):
```bash
npm run android
```


## 📱 Màn hình

- **HomeScreen** - Danh sách sản phẩm, lọc theo category
- **LoginScreen** - Đăng nhập
- **RegisterScreen** - Đăng ký tài khoản
- **CartScreen** - Giỏ hàng với tính tổng
- **ProfileScreen** - Thông tin user, đăng xuất
- **ProductDetailScreen** - Chi tiết sản phẩm (đang phát triển)
- **OrdersScreen** - Danh sách đơn hàng (đang phát triển)
- **CheckoutScreen** - Thanh toán (đang phát triển)

## 🎨 UI/UX

- **Theme**: Sky blue gradient (#0ea5e9)
- **Navigation**: Bottom tabs (Home, Cart, Profile)
- **Animations**: Smooth transitions
- **Responsive**: Tương thích mọi kích thước màn hình

## 📦 Build APK

```bash
# Build APK cho Android
eas build --platform android --profile preview
```

## 📝 Cấu trúc thư mục

```
food-shop-mobile/
├── App.js                    # Entry point
├── app.json                  # Expo config
├── package.json              # Dependencies
├── babel.config.js           # Babel config
├── src/
│   ├── config/
│   │   └── api.js           # API configuration
│   ├── context/
│   │   ├── AuthContext.js   # Authentication context
│   │   └── CartContext.js   # Cart context
│   ├── navigation/
│   │   └── AppNavigator.js  # Navigation setup
│   └── screens/
│       ├── HomeScreen.js
│       ├── LoginScreen.js
│       ├── RegisterScreen.js
│       ├── CartScreen.js
│       └── ProfileScreen.js
└── README.md
```

## 👨‍💻 Tác giả

**haivoDA22TTD (haivoDev)**
- 📧 Email: 110122068@st.tvu.edu.vn
- 👤 GitHub: [@haivoDA22TTD](https://github.com/haivoDA22TTD)
- 🎓 Sinh viên Đại học Trà Vinh

---

**Status:** ✅ Hoàn thành cơ bản - Đang phát triển thêm tính năng!
