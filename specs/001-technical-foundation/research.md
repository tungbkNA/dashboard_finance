# Research: Nền Móng Kỹ Thuật Ban Đầu

**Feature**: 001-technical-foundation
**Phase**: 0 — Resolve NEEDS CLARIFICATION & best practices
**Date**: 2026-05-06

---

## R-001: Spring Boot version cho Java 21 + springdoc-openapi

**Decision**: Spring Boot **3.4.5** + springdoc-openapi-starter-webmvc-ui **2.8.6**

**Rationale**:
- Spring Boot 3.4.x là latest stable line (May 2026), Java 21 là first-class target (virtual threads, record patterns)
- springdoc-openapi **2.x** là bắt buộc cho Spring Boot 3.x; phiên bản 1.x chỉ hỗ trợ Spring Boot 2.x
- springdoc-openapi 2.8.6 là latest stable tương thích với Spring Boot 3.4.x
- Cả hai có trong Maven Central

**Alternatives considered**:
- Spring Boot 3.3.x — vẫn được hỗ trợ nhưng 3.4.x có cải tiến tốt hơn cho Java 21

**pom.xml dependencies**:
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.4.5</version>
</parent>

<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.8.6</version>
</dependency>
```

---

## R-002: PrimeVue 4.x — cấu hình red theme

**Decision**: PrimeVue **4.3.x** với **Aura** preset, primary palette `red`

**Rationale**:
- PrimeVue 4.x (released late 2024) dùng CSS custom properties — không cần Tailwind, không cần SASS
- `definePreset` API cho phép override `semantic.primary` với built-in `red` palette của PrimeVue
- Aura là preset hiện đại, clean, phù hợp cho internal tool

**Alternatives considered**:
- PrimeVue 3.x — theming qua SASS variables, ít linh hoạt hơn; migration cost cao nếu nâng cấp sau

**Setup code** (main.ts):
```ts
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import { definePreset } from '@primevue/themes'

const RedPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50:  '{red.50}',
      100: '{red.100}',
      200: '{red.200}',
      300: '{red.300}',
      400: '{red.400}',
      500: '{red.500}',
      600: '{red.600}',
      700: '{red.700}',
      800: '{red.800}',
      900: '{red.900}',
      950: '{red.950}'
    }
  }
})

app.use(PrimeVue, { theme: { preset: RedPreset, options: { darkModeSelector: false } } })
```

**Package versions**:
- `primevue`: `^4.3.0`
- `@primevue/themes`: `^4.3.0`
- `primeicons`: `^7.0.0`

---

## R-003: Docker Compose — backend service only

**Decision**: `docker-compose.yml` tại repo root chạy **backend service only**. PostgreSQL là local installation.

**Rationale**:
- Developer đã clarify: PostgreSQL là local DB, không qua Docker (Clarification Q4)
- Chạy backend trong Docker với PostgreSQL local cần `host.docker.internal` (hoạt động tốt trên macOS/Windows Docker Desktop; trên Linux cần thêm `--add-host`)
- README sẽ hướng dẫn cả 2 cách: Maven (đơn giản hơn cho dev) và Docker Compose

**Docker Compose structure** (docker-compose.yml):
```yaml
services:
  backend:
    build:
      context: ./BE
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SPRING_DATASOURCE_URL: jdbc:postgresql://host.docker.internal:5432/dashboard_finance
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    extra_hosts:
      - "host.docker.internal:host-gateway"  # Linux compatibility
```

**Alternatives considered**:
- Include PostgreSQL in Docker Compose — rejected per developer clarification Q4

---

## R-004: FE testing — scope decision

**Decision**: **Không setup FE test** trong feature này

**Rationale**:
- Feature scope là foundation scaffold only (FR-INT-003)
- Thêm Vitest config không có test nào là dead configuration
- Vitest + Vue Test Utils sẽ thêm khi có business feature đầu tiên cần FE unit test

**Follow-up TODO**: Khi tạo spec cho feature nghiệp vụ đầu tiên, thêm Vitest setup task

---

## R-005: Java package root & Maven artifact naming

**Decision**: groupId `com.internal`, artifactId `dashboard-finance`, package root `com.internal.projectmgmt`

**Rationale**:
- `dashboard-finance` khớp với `service` field trong health check response (`"service": "dashboard-finance"`)
- Package `com.internal.projectmgmt` phân biệt rõ đây là internal project management system

**Alternatives considered**:
- `com.company.dashboard` — quá chung chung
- `vn.company.projectmgmt` — domain-specific prefix, thay đổi được nếu team muốn

---

## R-006: Flyway initial migration strategy

**Decision**: `V1__init_schema.sql` với nội dung comment placeholder; không tạo bảng nghiệp vụ

**Rationale**:
- Flyway cần ít nhất 1 migration để thiết lập `flyway_schema_history`
- Business tables thuộc về feature specs sau (mỗi feature thêm migration riêng)
- Versioning: V1 = foundation, V2+ = feature migrations

**Migration content**:
```sql
-- V1: Initial schema baseline
-- Dashboard Finance — Internal Project Management System
-- Business tables will be added in subsequent feature migrations
-- Feature: 001-technical-foundation
```

---

## R-007: Axios interceptors cho error handling thống nhất

**Decision**: Global Axios response interceptor bắt lỗi 4xx/5xx và emit toast notification

**Rationale**:
- FR-FE-007 yêu cầu 100% lỗi được hiển thị (SC-004)
- Interceptor tập trung tránh mỗi service phải tự xử lý lỗi

**Pattern**:
```ts
// services/api.ts
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message ?? 'Không thể kết nối đến máy chủ'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: message, life: 4000 })
    return Promise.reject(error)
  }
)
```

**Note**: Toast service (PrimeVue `useToast`) cần được inject vào `api.ts` qua Pinia store hoặc event emitter để tránh circular dependency.

---

## Summary — All NEEDS CLARIFICATION resolved

| Item | Resolution |
|---|---|
| Testing (FE) | Not in scope; Vitest added in first business feature |
| Performance Goals | SC-003: health check < 500ms |
| Scale/Scope | Foundation scaffold; internal tool |
| Spring Boot version | 3.4.5 |
| springdoc-openapi version | 2.8.6 |
| PrimeVue version + red theme | 4.3.x + Aura preset + red palette |
| Docker Compose contents | Backend only; PostgreSQL is local |
| Flyway migration | V1 placeholder; no business tables |
| Axios error handling | Global interceptor → toast |
