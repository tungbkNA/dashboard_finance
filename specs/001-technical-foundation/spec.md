# Feature Specification: Nền Móng Kỹ Thuật Ban Đầu (Technical Foundation)

**Feature Branch**: `001-technical-foundation`
**Created**: 2026-05-06
**Status**: Draft
**Governing Constitution**: Hiến Chương Hệ Thống Quản Lý Dự Án Nội Bộ (v1.0.0)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 – Lập Trình Viên Khởi Động Hệ Thống Lần Đầu (Priority: P1)

Là một lập trình viên mới tham gia dự án, tôi muốn có thể khởi chạy toàn bộ hệ thống (BE + FE + database) trên máy local trong vòng vài phút mà không cần cấu hình thủ công phức tạp, để tôi có thể bắt đầu phát triển nghiệp vụ ngay lập tức.

**Why this priority**: Đây là điều kiện tiên quyết cho mọi phát triển tiếp theo. Nếu hệ thống không khởi động được, không ai làm việc được.

**Independent Test**: Mở terminal, tạo DB local theo README, start BE (`mvn spring-boot:run -Plocal`), start FE, mở trình duyệt và thấy layout chính với menu hiển thị.

**Acceptance Scenarios**:

1. **Given** máy đã cài Java 21, Maven, Node.js, Docker, và đã tạo sẵn PostgreSQL DB local theo hướng dẫn README, **When** lập trình viên chạy đúng lệnh theo README, **Then** backend khởi động thành công qua Docker Compose và health check endpoint trả về trạng thái OK.
2. **Given** backend đang chạy, **When** lập trình viên chạy lệnh start frontend, **Then** giao diện mở trong trình duyệt, layout chính hiển thị với sidebar, header và content area.
3. **Given** cả BE và FE đang chạy, **When** frontend tải, **Then** frontend gọi health check của backend và không hiển thị gì (kết nối thành công là trạng thái im lặng).

---

### User Story 2 – Lập Trình Viên Kiểm Tra Cấu Trúc Dự Án (Priority: P2)

Là một lập trình viên, tôi muốn thấy cấu trúc thư mục và package rõ ràng ở cả BE và FE, để tôi biết đặt code mới ở đâu khi xây dựng tính năng nghiệp vụ.

**Why this priority**: Cấu trúc rõ ràng ngay từ đầu giúp toàn team phát triển đồng nhất và tránh code lộn xộn sau này.

**Independent Test**: Mở IDE, duyệt thư mục BE/ và FE/, xác nhận đủ các tầng/package đã khai báo trong constitution.

**Acceptance Scenarios**:

1. **Given** repository đã được clone, **When** lập trình viên mở thư mục BE/, **Then** thấy cấu trúc package bao gồm: controller, service, repository, dto, entity, mapper, validation, exception, config.
2. **Given** repository đã được clone, **When** lập trình viên mở thư mục FE/src/, **Then** thấy cấu trúc bao gồm: components, views, router, stores, services.
3. **Given** backend đang chạy, **When** lập trình viên truy cập đường dẫn API docs, **Then** Swagger UI hiển thị danh sách endpoint có sẵn.

---

### User Story 3 – Lập Trình Viên Xử Lý Lỗi Từ API (Priority: P3)

Là một lập trình viên frontend, tôi muốn khi backend trả về lỗi, frontend hiển thị thông báo lỗi thân thiện với người dùng và không bị treo trang, để trải nghiệm người dùng không bị gián đoạn ngay cả khi có sự cố.

**Why this priority**: Xử lý lỗi cơ bản cần có ngay từ đầu để toàn team không phải xử lý thủ công trong từng tính năng sau này.

**Independent Test**: Tắt backend, mở frontend, quan sát màn hình hiển thị error state thay vì bị trắng/crash.

**Acceptance Scenarios**:

1. **Given** backend không khả dụng, **When** frontend gọi health check, **Then** hiển thị toast notification lỗi với thông báo rõ ràng (ví dụ: "Không thể kết nối đến máy chủ") thay vì lỗi trống trơn.
2. **Given** backend trả về lỗi (4xx/5xx), **When** frontend nhận response, **Then** hiển thị toast notification với thông báo lỗi người dùng có thể đọc được.
3. **Given** API đang được gọi, **When** đang chờ response, **Then** loading indicator hiển thị và UI không bị đơ.

---

### Edge Cases

- Điều gì xảy ra nếu PostgreSQL chưa khởi động khi backend start? → Backend phải báo lỗi rõ ràng ở log, không crash im lặng.
- Điều gì xảy ra nếu cổng mặc định của BE hoặc FE đã bị chiếm? → README phải hướng dẫn cách đổi port.
- Điều gì xảy ra nếu biến môi trường chưa được cấu hình? → Backend phải có giá trị mặc định cho local development.
- Điều gì xảy ra khi frontend không thể kết nối BE do lỗi CORS? → CORS phải được cấu hình sẵn cho localhost.

---

## Requirements *(mandatory)*

### Functional Requirements

**Backend:**

- **FR-BE-001**: Hệ thống PHẢI có cấu trúc package bao gồm đủ các tầng: `controller`, `service`, `repository`, `dto`, `entity`, `mapper`, `validation`, `exception`, `config`.
- **FR-BE-002**: Backend PHẢI cung cấp health check endpoint `GET /api/binance/health` trả về response chuẩn: `{ "code": "SUCCESS", "message": "OK", "data": { "status": "UP", "service": "dashboard-finance", "version": "1.0.0" } }`.
- **FR-BE-003**: Backend PHẢI có global exception handler trả về response lỗi theo định dạng chuẩn `{ "code": "...", "message": "...", "data": null }`.
- **FR-BE-004**: Backend PHẢI có cấu hình CORS cho phép frontend chạy trên localhost gọi API.
- **FR-BE-005**: Backend PHẢI có cấu hình profile cho môi trường local development (`application-local.properties` hoặc tương đương).
- **FR-BE-006**: Backend PHẢI tích hợp Flyway để quản lý database migration, với ít nhất một migration khởi tạo schema.
- **FR-BE-007**: Backend PHẢI tích hợp springdoc-openapi để tự động sinh API documentation, truy cập qua Swagger UI.
- **FR-BE-008**: Backend PHẢI có cấu trúc thư mục test sẵn sàng (unit test + integration test) dù chưa có test nghiệp vụ.
- **FR-BE-009**: Backend PHẢI cấu hình log level cho local development: `INFO` cho application code; `ERROR` cho các thư viện ngoài (ví dụ: Hibernate, Spring framework).

**Frontend:**

- **FR-FE-001**: Frontend PHẢI được thiết lập với Vue 3, Vite, TypeScript, Vue Router, Pinia, PrimeVue và PrimeIcons.
- **FR-FE-002**: Frontend PHẢI sử dụng tông màu đỏ làm màu chủ đạo của PrimeVue theme.
- **FR-FE-003**: Frontend PHẢI có layout chính bao gồm: sidebar navigation, header và content area.
- **FR-FE-004**: Sidebar PHẢI có menu ban đầu gồm 4 mục: Dashboard, Cài đặt dự án, Quản Lý Các Dự Án, Cấu hình — mỗi mục có icon PrimeIcons phù hợp.
- **FR-FE-005**: Frontend PHẢI có API client (axios hoặc fetch wrapper) được cấu hình base URL để gọi backend, đọc URL từ biến môi trường.
- **FR-FE-006**: Frontend PHẢI có các state UI cơ bản tái sử dụng: loading state, empty state và error state.
- **FR-FE-007**: Frontend PHẢI gọi `GET /api/binance/health` khi ứng dụng tải; nếu thất bại, hiển thị toast notification lỗi; nếu thành công, không hiển thị gì (silent success).

**Tích hợp & Hạ tầng:**

- **FR-INT-001**: Repository PHẢI có file `docker-compose.yml` khởi động backend service. PostgreSQL không chạy qua Docker — developer tự tạo DB local trước khi chạy backend.
- **FR-INT-002**: Repository PHẢI có README hoặc quickstart hướng dẫn từng bước: tạo PostgreSQL DB local, cấu hình kết nối, chạy BE (qua Docker Compose hoặc Maven), chạy FE.
- **FR-INT-003**: KHÔNG được triển khai bất kỳ nghiệp vụ quản lý dự án nào trong feature này.

### Key Entities

- **HealthStatus**: Trạng thái hệ thống trả về từ `GET /api/binance/health`. Các trường: `status` (UP | DOWN), `service` (string — tên service, giá trị cố định `dashboard-finance`), `version` (string — phiên bản hiện tại của backend). Bọc trong response chuẩn `{ code, message, data }`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Lập trình viên mới có thể khởi chạy toàn bộ hệ thống (DB + BE + FE) trong vòng 10 phút chỉ theo README, không cần hỗ trợ thêm.
- **SC-002**: Frontend hiển thị layout chính với sidebar và menu đầy đủ trong vòng 3 giây sau khi tải trang.
- **SC-003**: Health check endpoint trả về response trong dưới 500ms trong điều kiện bình thường.
- **SC-004**: 100% lỗi từ backend được bắt và hiển thị thông báo thân thiện ở frontend — không có lỗi nào bị nuốt im lặng.
- **SC-005**: API documentation truy cập được ngay sau khi backend khởi động, liệt kê đủ các endpoint hiện có.
- **SC-006**: Cấu trúc thư mục BE và FE khớp với quy định trong constitution, được xác nhận bằng code review checklist.

---

## Clarifications

### Session 2026-05-06

- Q: Tất cả REST API endpoints trong hệ thống này dùng URL prefix nào? → A: `/api/binance`
- Q: Khi frontend tải và gọi health check, trạng thái kết nối BE hiển thị ở đâu? → A: Toast notification — chỉ hiển thị khi có lỗi kết nối (kết nối thành công không hiển thị gì)
- Q: `GET /api/binance/health` trả về response body dạng nào? → A: `{ "status": "UP", "service": "dashboard-finance", "version": "1.0.0" }`
- Q: Docker Compose trong repo này khởi động những service nào? → A: Backend only. PostgreSQL là DB local do developer tự tạo trước, không chạy qua Docker.
- Q: Backend log ra console ở mức nào khi chạy local development? → A: `INFO` cho application code; `ERROR` cho các thư viện ngoài (giảm noise).

---

## Assumptions

- Máy phát triển đã cài sẵn Java 21, Maven 3.9+, Node.js 20+, Docker Desktop.
- Database là PostgreSQL local (không qua Docker) — developer tự cài và tạo DB trước khi chạy backend. Tên DB, user, password mặc định được quy định trong README.
- Không có yêu cầu xác thực/phân quyền trong feature này; bảo mật sẽ được thêm trong feature riêng.
- Frontend và backend chạy trên các cổng mặc định khác nhau (ví dụ: BE 8080, FE 5173) trong môi trường local.
- Tất cả cấu hình nhạy cảm (password DB, secret key) dùng giá trị mặc định cho local; production config nằm ngoài scope.
- Pinia được thiết lập sẵn nhưng chưa cần store nghiệp vụ trong feature này.
- Log level local: `INFO` cho application; `ERROR` cho thư viện ngoài.
