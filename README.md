## 🎯 Tổng Quan Eureka Server

Eureka Server là service registry cho kiến trúc microservices. Tất cả services sẽ register với Eureka để discover và giao tiếp với nhau.

```
                    ┌─────────────────┐
                    │ Eureka Server   │
                    │   (Port 8761)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┬──────────────┐
        │                    │                    │              │
   ┌────▼─────┐      ┌──────▼──────┐     ┌──────▼──────┐  ┌───▼──────┐
   │ Identity │      │  Product    │     │   Order     │  │ Payment  │
   │ Service  │      │  Service    │     │  Service    │  │ Service  │
   │ (8081)   │      │  (8082)     │     │  (8083)     │  │ (8084)   │
   └──────────┘      └─────────────┘     └─────────────┘  └──────────┘
```