# Data Model: Nền Móng Kỹ Thuật Ban Đầu

**Feature**: 001-technical-foundation
**Phase**: 1 — Design
**Date**: 2026-05-06
**Spec Reference**: specs/001-technical-foundation/spec.md

---

## Overview

Feature này **không có database entity**. Không có bảng nào được tạo cho feature này.

Cấu trúc duy nhất là các DTO dùng trong response của health check endpoint.

---

## Response DTOs

### ApiResponse\<T\> — Generic Response Wrapper

Dùng cho **tất cả** endpoints trong hệ thống (constitution §6.3). Không phải entity database.

| Field | Java Type | Nullable | Description |
|---|---|---|---|
| `code` | `String` | No | `"SUCCESS"` hoặc error code (ví dụ: `"INTERNAL_ERROR"`, `"VALIDATION_ERROR"`) |
| `message` | `String` | No | Thông báo có thể đọc được |
| `data` | `T` (generic) | Yes | Payload; `null` cho error responses |

**Vị trí**: `com.internal.projectmgmt.dto.ApiResponse<T>`

**Static factory methods** (gợi ý):
```java
ApiResponse.success(T data)           // code=SUCCESS, data=data
ApiResponse.success(String message, T data)
ApiResponse.error(String code, String message)  // data=null
```

---

### HealthStatusDto

Dùng làm `data` field trong `ApiResponse<HealthStatusDto>` của `GET /api/binance/health`.
Không phải entity database — là pure DTO.

| Field | Java Type | Nullable | Value | Description |
|---|---|---|---|---|
| `status` | `String` | No | `"UP"` \| `"DOWN"` | Trạng thái hệ thống |
| `service` | `String` | No | `"dashboard-finance"` (cố định) | Tên service |
| `version` | `String` | No | Lấy từ `application.properties` (e.g. `"1.0.0"`) | Phiên bản backend |

**Vị trí**: `com.internal.projectmgmt.dto.HealthStatusDto`

---

## Database Migrations

### V1__init_schema.sql

Flyway migration đầu tiên — thiết lập `flyway_schema_history` baseline. Không tạo business table.

```sql
-- V1: Initial schema baseline
-- Dashboard Finance — Internal Project Management System
-- Business tables will be added in subsequent feature migrations
-- Feature: 001-technical-foundation
```

**Vị trí**: `BE/src/main/resources/db/migration/V1__init_schema.sql`

**Naming convention cho migrations sau**:
- `V2__` — feature tiếp theo tạo bảng nghiệp vụ đầu tiên
- Mỗi feature spec phải khai báo migration của nó

---

## Entity Conventions (cho future features)

Các entity nghiệp vụ (chưa có trong feature này) PHẢI tuân theo:

- `id`: `UUID` — generated (`@GeneratedValue(strategy = GenerationType.UUID)`)
- Không expose composite key ra ngoài (constitution §7.1)
- Dùng `BigDecimal` cho tiền, đơn giá, số lượng quan trọng (constitution §4.3)
- Audit fields: `createdAt`, `updatedAt` (`@CreationTimestamp`, `@UpdateTimestamp`) — optional, thêm khi cần

---

## State Transitions

Không có trong feature này.

---

## Validation Rules

Không có business validation trong feature này.

Global exception handler (`@RestControllerAdvice`) bắt:
- `MethodArgumentNotValidException` → response `ApiResponse.error("VALIDATION_ERROR", ...)`
- `Exception` (catch-all) → response `ApiResponse.error("INTERNAL_ERROR", "Lỗi hệ thống nội bộ")`
