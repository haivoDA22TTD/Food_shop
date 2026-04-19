# 🎯 Tổng Quan API Gateway

API Gateway là cổng vào duy nhất cho tất cả requests từ Frontend đến các microservices.

---

## 📋 Vai Trò

- **Entry Point:** Cổng vào duy nhất cho Frontend
- **Routing:** Chuyển requests đến đúng service
- **Service Discovery:** Tìm services qua Eureka
- **Load Balancing:** Phân tải tự động
- **CORS:** Cho phép Frontend gọi API

---

## 🔧 Công Nghệ

- **Spring Cloud Gateway** - Core routing engine
- **Spring Cloud Netflix Eureka Client** - Service discovery
- **Spring Boot Actuator** - Health check & monitoring
- **Reactive WebFlux** - Non-blocking I/O

---

## 📊 Sơ Đồ Kiến Trúc

```
                    ┌─────────────────┐
                    │   Frontend      │
                    │ React + Vite    │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  API Gateway    │
                    │   (Port 8080)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ Eureka Server   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┬──────────────┐
        │                    │                    │              │
   ┌────▼─────┐      ┌──────▼──────┐     ┌──────▼──────┐  ┌───▼──────┐
   │ Identity │      │  Product    │     │   Order     │  │ Payment  │
   │ Service  │      │  Service    │     │  Service    │  │ Service  │
   │ (8081)   │      │  (8082)     │     │  (8083)     │  │ (8084)   │
   └──────────┘      └─────────────┘     └─────────────┘  └──────────┘
