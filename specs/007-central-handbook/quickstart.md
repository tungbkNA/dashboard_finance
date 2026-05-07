# Quickstart: Sổ tay trung tâm

**Feature**: 007-central-handbook

## Prerequisites

- Java 21, Maven, PostgreSQL 15+ running
- Node.js 20+, npm
- Backend & Frontend already set up from previous features

## 1. Run Flyway Migrations

```bash
cd BE
mvn flyway:migrate
# Or just start the backend — Spring Boot auto-applies pending migrations
```

This creates:
- `file_group` table with 3 seed records (Kế hoạch tháng, Báo cáo, Quy trình)
- `file_record` table
- `MANAGE_HANDBOOK` permission assigned to admin role

## 2. Start Backend

```bash
cd BE
mvn spring-boot:run
```

## 3. Start Frontend

```bash
cd FE
npm run dev
```

## 4. Verify

1. Login as `admin` / `123456aA@`
2. Sidebar should show "Sổ tay trung tâm" section with 2 sub-items
3. Navigate to "Quản lý nhóm file" — should see 3 default groups
4. Navigate to "Danh mục file" — should be empty with "Chưa có dữ liệu" message
5. Create a file record with a test URL — verify it appears and link opens in new tab

## API Quick Test

```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456aA@"}' | jq -r '.data.token')

# List file groups
curl -s http://localhost:8080/api/handbook/file-groups \
  -H "Authorization: Bearer $TOKEN" | jq

# Create a file record
curl -s -X POST http://localhost:8080/api/handbook/file-records \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"fileName":"Test Doc","fileUrl":"https://example.com","groupId":"<group-id-from-above>"}' | jq
```

## Key Files

| Layer | Files |
|-------|-------|
| Migration | `BE/src/main/resources/db/migration/V10__*.sql`, `V11__*.sql` |
| Entity | `BE/.../entity/FileGroup.java`, `FileRecord.java` |
| Repository | `BE/.../repository/FileGroupRepository.java`, `FileRecordRepository.java` |
| Service | `BE/.../service/FileGroupService.java`, `FileRecordService.java` |
| Controller | `BE/.../controller/FileGroupController.java`, `FileRecordController.java` |
| DTO | `BE/.../dto/filegroup/*.java`, `BE/.../dto/filerecord/*.java` |
| Mapper | `BE/.../mapper/FileGroupMapper.java`, `FileRecordMapper.java` |
| Test | `BE/.../service/FileGroupServiceTest.java` |
| FE Views | `FE/src/views/handbook/FileGroupView.vue`, `FileListView.vue` |
| FE Dialogs | `FE/src/components/handbook/FileGroupDialog.vue`, `FileRecordDialog.vue` |
| FE Services | `FE/src/services/fileGroupService.ts`, `fileRecordService.ts` |
| FE Types | `FE/src/types/handbook.ts` |
| FE Router | `FE/src/router/index.ts` (updated) |
| FE Sidebar | `FE/src/components/AppSidebar.vue` (updated) |
