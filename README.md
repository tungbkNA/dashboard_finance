# Dashboard Finance — Hệ Thống Quản Lý Dự Án Nội Bộ

## Giới thiệu

Hệ thống quản lý dự án nội bộ, bao gồm:
- **BE/**: Backend Spring Boot 3.4.5 / Java 21 / Maven
- **FE/**: Frontend Vue 3 / Vite / TypeScript / PrimeVue

---

## Yêu cầu môi trường

| Công cụ | Phiên bản tối thiểu |
|---------|---------------------|
| Java    | 21                  |
| Maven   | 3.9+                |
| Node.js | 20+                 |
| Docker Desktop | (tuỳ chọn, để chạy BE qua Docker Compose) |
| PostgreSQL | 15 (cài local, không qua Docker) |

---

## Hướng dẫn khởi động (5 bước)

### Bước 1 — Tạo PostgreSQL database local

Mở `psql` hoặc bất kỳ tool nào (pgAdmin, DBeaver) và chạy:

```sql
CREATE DATABASE dashboard_finance;
```

Thông tin kết nối mặc định:

| Trường   | Giá trị             |
|----------|---------------------|
| Host     | `localhost`         |
| Port     | `5432`              |
| Database | `dashboard_finance` |
| Username | `postgres`          |
| Password | `postgres`          |

> Để thay đổi, sửa `BE/src/main/resources/application-local.properties`.

---

### Bước 2 — Chạy Backend

**Cách 1 — Maven (khuyến nghị khi dev):**

```bash
cd BE
mvn spring-boot:run -Plocal
```

**Cách 2 — Docker Compose:**

```bash
# Build JAR trước
cd BE && mvn package -DskipTests && cd ..
# Chạy container
docker-compose up --build
```

> Backend mặc định chạy trên cổng **8080**.
> Để đổi port: thêm `server.port=XXXX` vào `BE/src/main/resources/application-local.properties`
> và sửa cổng trong `docker-compose.yml`.

---

### Bước 3 — Kiểm tra Backend

```bash
curl http://localhost:8080/api/binance/health
```

Kết quả mong đợi:

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "status": "UP",
    "service": "dashboard-finance",
    "version": "1.0.0"
  }
}
```

API Documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### Bước 4 — Chạy Frontend

```bash
cd FE
npm install
npm run dev
```

> Frontend chạy trên cổng **5173** mặc định.
> Để đổi port: sửa `server.port` trong `FE/vite.config.ts`.

---

### Bước 5 — Truy cập ứng dụng

Mở trình duyệt: [http://localhost:5173](http://localhost:5173)

Giao diện hiển thị layout với:
- Sidebar navigation (4 menu: Dashboard, Cài đặt dự án, Quản Lý Các Dự Án, Cấu hình)
- Header với tông màu đỏ

---

## Xử lý sự cố thường gặp

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| Backend không khởi động | PostgreSQL chưa chạy hoặc DB chưa tạo | Kiểm tra PostgreSQL service, tạo DB theo Bước 1 |
| Port 8080 đã bị chiếm | Ứng dụng khác đang dùng port | Đổi `server.port` trong `application-local.properties` |
| Port 5173 đã bị chiếm | Ứng dụng khác đang dùng port | Đổi `server.port` trong `vite.config.ts` |
| FE không gọi được BE | CORS hoặc URL sai | Kiểm tra `VITE_API_BASE_URL` trong `FE/.env.local` |
| Flyway migration thất bại | Schema history conflict | Xóa bảng `flyway_schema_history` và thử lại |

---

## Cấu trúc thư mục

```
.
├── BE/                    ← Spring Boot backend
│   ├── src/main/java/com/internal/projectmgmt/
│   │   ├── controller/    ← REST controllers
│   │   ├── service/       ← Business logic
│   │   ├── repository/    ← Spring Data JPA repos
│   │   ├── dto/           ← Data Transfer Objects
│   │   ├── entity/        ← JPA entities
│   │   ├── mapper/        ← Object mappers
│   │   ├── validation/    ← Custom validators
│   │   ├── exception/     ← Exception handlers
│   │   └── config/        ← Spring configs
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-local.properties
│       └── db/migration/  ← Flyway migrations
├── FE/                    ← Vue 3 frontend
│   └── src/
│       ├── components/    ← Reusable Vue components
│       ├── views/         ← Page components
│       ├── router/        ← Vue Router
│       ├── stores/        ← Pinia stores
│       ├── services/      ← API service layer
│       └── types/         ← TypeScript interfaces
├── docker-compose.yml     ← Backend service only
└── README.md              ← This file
```
