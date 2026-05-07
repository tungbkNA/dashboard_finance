# Data Model: Cài Đặt Dự Án (Feature 002)

**Date**: 2026-05-07
**Spec**: [spec.md](./spec.md) | **Research**: [research.md](./research.md)

---

## Entities

### 1. Project (Dự Án)

**Table**: `project`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT gen_random_uuid() | |
| `project_code` | `VARCHAR(50)` | NOT NULL | Unique via partial index LOWER(project_code) WHERE deleted=false |
| `project_name` | `VARCHAR(255)` | NOT NULL | |
| `represent_id` | `UUID` | NULL | No FK — personnel table not yet created |
| `customer_id` | `UUID` | NOT NULL, FK → customer(id) | |
| `project_type_id` | `UUID` | NOT NULL, FK → project_type(id) | |
| `price` | `NUMERIC(19,4)` | NOT NULL, DEFAULT 0, CHECK ≥ 0 | Maps to BigDecimal |
| `status_contract` | `project_status_contract` | NOT NULL | Enum: NO_CONTRACT, HAS_CONTRACT |
| `status_project` | `project_status` | NOT NULL | Enum: OPEN, INPROGRESS, PENDING, DONE, CLOSE |
| `month_start` | `VARCHAR(7)` | NOT NULL | Format: mm/yyyy |
| `month_end` | `VARCHAR(7)` | NOT NULL | Format: mm/yyyy |
| `deleted` | `BOOLEAN` | NOT NULL, DEFAULT false | Soft delete flag |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT now() | |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT now() | Updated via trigger |

**Indexes**:
```sql
CREATE UNIQUE INDEX idx_project_code_lower ON project(LOWER(project_code)) WHERE deleted = false;
```

**Validation Rules**:
- `project_code`: matches `^[A-Za-z0-9_-]{1,50}$`, case-insensitive unique (partial index)
- `price`: BigDecimal ≥ 0
- `month_start`, `month_end`: matches `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`
- `month_end` ≥ `month_start` (compared as yyyymm integer after parsing)
- `customer_id` must reference existing non-deleted customer
- `project_type_id` must reference existing non-deleted project_type

**State Transitions**:
- `status_project` and `status_contract` — no transition constraints, freely editable

---

### 2. ProjectType (Loại Dự Án)

**Table**: `project_type`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT gen_random_uuid() | |
| `key` | `VARCHAR(50)` | NOT NULL | Unique via partial index LOWER(key) WHERE deleted=false |
| `value` | `VARCHAR(255)` | NOT NULL | Display name |
| `deleted` | `BOOLEAN` | NOT NULL, DEFAULT false | Soft delete flag |

**Indexes**:
```sql
CREATE UNIQUE INDEX idx_project_type_key_lower ON project_type(LOWER(key)) WHERE deleted = false;
```

**Validation Rules**:
- `key`: matches `^[A-Za-z0-9_-]{1,50}$`, case-insensitive unique
- `value`: not blank

**Delete Behavior**:
- Soft delete always (`deleted = true`)
- If referenced by ≥ 1 project: warn user, then soft delete on confirmation
- If not referenced: soft delete on confirmation

---

### 3. Customer (Khách Hàng)

**Table**: `customer`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT gen_random_uuid() | |
| `customer_code` | `VARCHAR(50)` | NOT NULL | Unique via partial index LOWER(customer_code) WHERE deleted=false |
| `customer_name` | `VARCHAR(255)` | NOT NULL | |
| `deleted` | `BOOLEAN` | NOT NULL, DEFAULT false | Soft delete flag |

**Indexes**:
```sql
CREATE UNIQUE INDEX idx_customer_code_lower ON customer(LOWER(customer_code)) WHERE deleted = false;
```

**Validation Rules**:
- `customer_code`: matches `^[A-Za-z0-9_-]{1,50}$`, case-insensitive unique
- `customer_name`: not blank

**Delete Behavior**: Same as ProjectType.

---

## PostgreSQL Enum Types

```sql
CREATE TYPE project_status AS ENUM ('OPEN', 'INPROGRESS', 'PENDING', 'DONE', 'CLOSE');
CREATE TYPE project_status_contract AS ENUM ('NO_CONTRACT', 'HAS_CONTRACT');
```

---

## Entity Relationships

```
ProjectType (1) ←── (N) Project (N) ──→ (1) Customer
                         │
                    represent_id (UUID, nullable, no FK)
```

- One ProjectType → Many Projects
- One Customer → Many Projects
- Project references ProjectType and Customer by UUID FK
- Soft-deleted ProjectType/Customer remain in DB; their FKs in Project remain valid

---

## Java Enums

```java
// StatusProject.java
public enum StatusProject {
    OPEN, INPROGRESS, PENDING, DONE, CLOSE
}

// StatusContract.java
public enum StatusContract {
    NO_CONTRACT,   // displayed as "Chưa có hợp đồng" (value=0)
    HAS_CONTRACT   // displayed as "Có hợp đồng"      (value=1)
}
```

---

## Flyway Migration: V2__project_settings_schema.sql

```sql
-- V2: Feature 002 — Cài đặt dự án
-- Creates project_type, customer, project tables with soft delete

CREATE TYPE project_status AS ENUM ('OPEN', 'INPROGRESS', 'PENDING', 'DONE', 'CLOSE');
CREATE TYPE project_status_contract AS ENUM ('NO_CONTRACT', 'HAS_CONTRACT');

CREATE TABLE project_type (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    key            VARCHAR(50) NOT NULL,
    value          VARCHAR(255) NOT NULL,
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_project_type_key_lower
    ON project_type(LOWER(key))
    WHERE deleted = FALSE;

CREATE TABLE customer (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_code  VARCHAR(50) NOT NULL,
    customer_name  VARCHAR(255) NOT NULL,
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_customer_code_lower
    ON customer(LOWER(customer_code))
    WHERE deleted = FALSE;

CREATE TABLE project (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_code     VARCHAR(50) NOT NULL,
    project_name     VARCHAR(255) NOT NULL,
    represent_id     UUID        NULL,
    customer_id      UUID        NOT NULL REFERENCES customer(id),
    project_type_id  UUID        NOT NULL REFERENCES project_type(id),
    price            NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (price >= 0),
    status_contract  project_status_contract NOT NULL,
    status_project   project_status NOT NULL,
    month_start      VARCHAR(7)  NOT NULL,
    month_end        VARCHAR(7)  NOT NULL,
    deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_project_code_lower
    ON project(LOWER(project_code))
    WHERE deleted = FALSE;
```

---

## DTO Summary

| DTO | Direction | Fields |
|-----|-----------|--------|
| `ProjectRequest` | FE → BE | projectCode, projectName, customerId, projectTypeId, price, statusContract, statusProject, monthStart, monthEnd |
| `ProjectResponse` | BE → FE | id, projectCode, projectName, customerId, customerName, projectTypeId, projectTypeName, price, statusContract, statusProject, monthStart, monthEnd, createdAt, updatedAt |
| `ProjectTypeRequest` | FE → BE | key, value |
| `ProjectTypeResponse` | BE → FE | id, key, value |
| `CustomerRequest` | FE → BE | customerCode, customerName |
| `CustomerResponse` | BE → FE | id, customerCode, customerName |

Note: Response DTOs include human-readable names (customerName, projectTypeName) to avoid extra FE lookups.
