# Research: Cài Đặt Dự Án (Feature 002)

**Date**: 2026-05-07
**Spec**: [spec.md](./spec.md)

---

## 1. Soft Delete Pattern — Spring Boot + PostgreSQL

**Decision**: Add `deleted BOOLEAN NOT NULL DEFAULT FALSE` column to all three tables (project, project_type, customer). All queries filter `WHERE deleted = false`. No physical deletes in this feature.

**Rationale**: Unified approach across all 3 entities simplifies code (same pattern everywhere) and aligns with FR-PRJ-009, FR-PT-005, FR-CUS-005 (soft delete for all). FK integrity preserved (project still references customer_id even after customer soft-deleted).

**Implementation pattern**:
- JPA: add `@Where(clause = "deleted = false")` on entity OR explicitly filter in repository queries
- Service layer checks: if deleting Customer/ProjectType that has linked projects → warn, but still soft-delete
- Prefer explicit `@Query` with `WHERE deleted = false` over `@Where` to avoid confusion with JOIN queries

---

## 2. Case-Insensitive Unique Constraint — PostgreSQL

**Decision**: Use PostgreSQL functional unique index: `CREATE UNIQUE INDEX ... ON table(LOWER(column))`.

**Rationale**: Native DB enforcement is stronger than application-level check (avoids race conditions). Spring `@Column(unique=true)` alone would be case-sensitive.

**Flyway SQL**:
```sql
CREATE UNIQUE INDEX idx_project_project_code_lower ON project(LOWER(project_code)) WHERE deleted = false;
CREATE UNIQUE INDEX idx_project_type_key_lower ON project_type(LOWER(key)) WHERE deleted = false;
CREATE UNIQUE INDEX idx_customer_code_lower ON customer(LOWER(customer_code)) WHERE deleted = false;
```

Note: partial index `WHERE deleted = false` means soft-deleted codes can be reused.

---

## 3. `mm/yyyy` Storage — String vs Date

**Decision**: Store as `VARCHAR(7)` in DB. Validate with regex `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$` in Bean Validation custom annotation.

**Rationale**: Spec explicitly says store as `mm/yyyy` string (not Date). Using VARCHAR avoids date parsing complexity. A custom `@MonthYear` annotation provides reusable validation.

---

## 4. Enum Strategy — PostgreSQL + JPA

**Decision**: Use PostgreSQL native `ENUM` type for `status_project` and `VARCHAR` with `@Enumerated(EnumType.STRING)` in JPA for `status_contract` (since it's 0/1 conceptually but stored as string enum label).

**Rationale**:
- `status_project`: 5 values (OPEN, INPROGRESS, PENDING, DONE, CLOSE) → PostgreSQL enum for constraint enforcement
- `status_contract`: 2 values (NO_CONTRACT=0, HAS_CONTRACT=1) → simple Java enum, serialize to string
- Flyway migration creates the PostgreSQL type before the table

---

## 5. FK Reference Strategy — project → customer/project_type

**Decision**: Project stores `customer_id UUID` FK referencing `customer(id)` and `project_type_id UUID` FK referencing `project_type(id)`. No `ON DELETE CASCADE` — soft delete handles this.

**Rationale**: Hard FK ensures referential integrity at DB level. Soft-deleted customers/project-types remain in DB so FKs remain valid.

---

## 6. API URL Prefix

**Decision**: Use `/api/binance` prefix (consistent with existing `HealthController`).

**Endpoints**:
- `/api/binance/projects` — CRUD projects
- `/api/binance/project-types` — CRUD project types
- `/api/binance/customers` — CRUD customers

---

## 7. Mapper Strategy — Manual vs MapStruct

**Decision**: Manual mapper classes in `mapper/` package.

**Rationale**: No MapStruct dependency in pom.xml. Adding it requires annotation processor config. Manual mappers are transparent, consistent with current empty `mapper/` stub.

---

## 8. FE State Management

**Decision**: Local component state (`ref`, `reactive`) — no Pinia store for this feature.

**Rationale**: Project/Customer/ProjectType data is screen-scoped; no cross-component sharing needed. Pinia reserved for truly global state (per existing `useAppStore` pattern).

---

## 9. FE Validation

**Decision**: Client-side validation using PrimeVue form + manual `v-if` error messages. Server errors displayed via toast (existing interceptor).

**Rationale**: BE is source of truth; FE does basic required/format validation for UX only. No separate validation library needed.

---

## 10. Test Strategy

**Decision**:
- Unit tests: Service layer (mocked repository) for uniqueness, validation, soft delete logic
- Integration tests: Repository layer with `@DataJpaTest` for constraint violations
- No FE tests in this feature (no Vitest setup yet)

**Test cases required by spec**:
1. Duplicate `projectCode` → throws AppException
2. Duplicate `customerCode` → throws AppException
3. Duplicate project type `key` → throws AppException
4. `monthEnd` < `monthStart` → throws AppException
5. Soft delete of Customer used by project → soft deletes with warning
6. Soft delete of ProjectType used by project → soft deletes with warning
