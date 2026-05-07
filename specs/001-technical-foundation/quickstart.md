# Quickstart: Hệ Thống Quản Lý Dự Án Nội Bộ

**Feature**: 001-technical-foundation
**Date**: 2026-05-06

---

## Yêu cầu tiên quyết

Đảm bảo máy đã cài đủ:

| Công cụ | Phiên bản tối thiểu | Kiểm tra |
|---|---|---|
| Java | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ | `node -version` |
| npm | 9+ | `npm -version` |
| Docker Desktop | Latest | `docker -v` |
| PostgreSQL | 15 (local) | `psql --version` |

---

## Bước 1: Clone repository

```bash
git clone <repo-url>
cd dashboard_finance
```

---

## Bước 2: Tạo PostgreSQL database local

Kết nối vào PostgreSQL local và tạo database:

```sql
CREATE DATABASE dashboard_finance;
-- Nếu dùng user riêng (tuỳ chọn):
-- CREATE USER dashboard_user WITH PASSWORD 'dashboard_pass';
-- GRANT ALL PRIVILEGES ON DATABASE dashboard_finance TO dashboard_user;
```

Hoặc dùng lệnh CLI:

```bash
psql -U postgres -c "CREATE DATABASE dashboard_finance;"
```

**Giá trị mặc định** (dùng cho local dev):

| Tham số | Giá trị mặc định |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `dashboard_finance` |
| Username | `postgres` |
| Password | `postgres` |

> Nếu PostgreSQL local của bạn dùng thông tin khác, cập nhật file `BE/src/main/resources/application-local.properties`.

---

## Bước 3: Chạy Backend

### Option A: Chạy trực tiếp với Maven (khuyến nghị cho development)

```bash
cd BE
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Backend khởi động tại: **http://localhost:8080**

Flyway tự động chạy migration khi backend start lần đầu.

### Option B: Chạy qua Docker Compose

> Lưu ý: Option này yêu cầu backend có thể kết nối PostgreSQL qua `host.docker.internal`. Hoạt động tốt trên macOS và Windows. Trên Linux, cần `--add-host` (đã có trong docker-compose.yml).

```bash
# Từ root của project
docker compose up --build
```

Backend khởi động tại: **http://localhost:8080**

Dừng:
```bash
docker compose down
```

---

## Bước 4: Kiểm tra Backend

Sau khi backend khởi động, kiểm tra health check:

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

API documentation (Swagger UI): **http://localhost:8080/swagger-ui/index.html**

---

## Bước 5: Chạy Frontend

```bash
cd FE
npm install
npm run dev
```

Frontend khởi động tại: **http://localhost:5173**

Mở trình duyệt, bạn sẽ thấy:
- Layout chính với sidebar bên trái
- Header ở trên
- Menu: Dashboard, Cài đặt dự án, Quản Lý Các Dự Án, Cấu hình

Nếu backend đang chạy, frontend kết nối thành công (không có thông báo gì = bình thường). Nếu backend không chạy, sẽ xuất hiện toast thông báo lỗi kết nối.

---

## Đổi port (nếu cần)

**Backend** — sửa `BE/src/main/resources/application-local.properties`:
```properties
server.port=8081
```

**Frontend** — sửa `FE/vite.config.ts`:
```ts
server: { port: 5174 }
```

Sau khi đổi port backend, cập nhật `FE/.env.local`:
```
VITE_API_BASE_URL=http://localhost:8081
```

---

## Cấu trúc thư mục nhanh

```
dashboard_finance/
├── BE/                  ← Spring Boot backend
│   ├── src/main/java/   ← Source code
│   ├── src/test/java/   ← Tests
│   └── pom.xml
├── FE/                  ← Vue 3 frontend
│   ├── src/
│   └── package.json
├── docker-compose.yml   ← Chạy backend qua Docker
└── specs/               ← Feature specifications
```

---

## Troubleshooting

**Backend không kết nối được PostgreSQL**:
- Kiểm tra PostgreSQL đang chạy: `pg_ctl status` hoặc `brew services list`
- Kiểm tra thông tin kết nối trong `application-local.properties`
- Kiểm tra log: `BE/logs/` hoặc terminal output

**CORS error khi frontend gọi backend**:
- Đảm bảo frontend chạy đúng port (mặc định 5173)
- Kiểm tra `CorsConfig.java` trong `BE/src/main/java/.../config/`

**Port đã bị chiếm**:
- Kiểm tra: `netstat -ano | findstr :8080` (Windows) hoặc `lsof -i :8080` (macOS/Linux)
- Đổi port theo hướng dẫn ở trên
