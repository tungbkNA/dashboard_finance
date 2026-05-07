# Data Model: Sổ tay trung tâm

**Feature**: 007-central-handbook  
**Date**: 2026-05-07

## Entity Relationship

```
FileGroup (1) ──────< (N) FileRecord
```

## Entities

### FileGroup

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Định danh duy nhất |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Tên nhóm file |
| description | VARCHAR(255) | nullable | Mô tả nhóm |
| active | BOOLEAN | NOT NULL, DEFAULT true | Trạng thái hoạt động |
| created_at | TIMESTAMPTZ | NOT NULL, auto | Ngày tạo |
| updated_at | TIMESTAMPTZ | NOT NULL, auto | Ngày cập nhật |

**Validation Rules**:
- `name`: bắt buộc, tối đa 100 ký tự, unique (case-insensitive)
- `description`: không bắt buộc, tối đa 255 ký tự

**State Transitions**:
- Active → Inactive: Cho phép. File thuộc nhóm sẽ bị ẩn khỏi danh sách mặc định.
- Inactive → Active: Cho phép. File thuộc nhóm sẽ hiển thị lại.
- Delete: Chỉ cho phép khi không có FileRecord nào tham chiếu.

---

### FileRecord

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Định danh duy nhất |
| file_name | VARCHAR(200) | NOT NULL | Tên file/tài liệu |
| file_url | VARCHAR(2048) | NOT NULL | Link URL (http/https) |
| file_group_id | UUID | NOT NULL, FK → file_group(id) | Nhóm file |
| created_by | VARCHAR(50) | NOT NULL | Người tạo (username) |
| created_at | TIMESTAMPTZ | NOT NULL, auto | Ngày tạo |
| updated_at | TIMESTAMPTZ | NOT NULL, auto | Ngày cập nhật |

**Validation Rules**:
- `file_name`: bắt buộc, tối đa 200 ký tự
- `file_url`: bắt buộc, phải bắt đầu bằng `http://` hoặc `https://`, tối đa 2048 ký tự
- `file_group_id`: bắt buộc, phải tham chiếu nhóm active (khi tạo mới)

**Relationships**:
- N-1 với FileGroup (mỗi file thuộc đúng 1 nhóm)
- FK constraint: `RESTRICT` on delete (không cho xóa nhóm nếu còn file)

---

## Flyway Migrations

### V10__file_group_and_record_schema.sql

```sql
-- V10: Create file_group and file_record tables for Central Handbook module

CREATE TABLE file_group (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_file_group_name UNIQUE (name)
);

CREATE TABLE file_record (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name       VARCHAR(200) NOT NULL,
    file_url        VARCHAR(2048) NOT NULL,
    file_group_id   UUID NOT NULL,
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_file_record_group FOREIGN KEY (file_group_id)
        REFERENCES file_group(id) ON DELETE RESTRICT
);

CREATE INDEX idx_file_record_group ON file_record(file_group_id);
CREATE INDEX idx_file_record_name  ON file_record(file_name);
```

### V11__seed_file_groups_and_permission.sql

```sql
-- V11: Seed default file groups and MANAGE_HANDBOOK permission

INSERT INTO file_group (id, name, description, active) VALUES
    (gen_random_uuid(), 'Kế hoạch tháng', 'Tài liệu kế hoạch hàng tháng', true),
    (gen_random_uuid(), 'Báo cáo', 'Các báo cáo tổng hợp', true),
    (gen_random_uuid(), 'Quy trình', 'Tài liệu quy trình nội bộ', true);

-- Add MANAGE_HANDBOOK permission
INSERT INTO permission (id, name, description)
VALUES (gen_random_uuid(), 'MANAGE_HANDBOOK', 'Quản lý Sổ tay trung tâm')
ON CONFLICT (name) DO NOTHING;

-- Assign to admin role (role with is_system = true)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.is_system = true AND p.name = 'MANAGE_HANDBOOK'
ON CONFLICT DO NOTHING;
```

## Java Entity Mapping

### FileGroup.java

```java
@Entity
@Table(name = "file_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileGroup {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = OffsetDateTime.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
```

### FileRecord.java

```java
@Entity
@Table(name = "file_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 2048)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id", nullable = false)
    private FileGroup fileGroup;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = OffsetDateTime.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
```
