# API Contract: Permissions

**Base path**: `/api/permissions`  
**Auth required**: ✅ (Bearer token)  
**Source**: [spec.md FR-009](../spec.md), [data-model.md](../data-model.md), [research.md R-010](../research.md)

---

## GET /api/permissions

Lấy toàn bộ danh sách Permission dưới dạng cây phân cấp. Dùng bởi FE để render giao diện tích quyền cho Role.

**Required permission**: `MANAGE_ROLE` (chỉ admin mới cần xem cây quyền)

**Response 200** — cấu trúc phẳng, FE tự build cây từ `parentCode`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "code": "VIEW_DASHBOARD",
      "displayName": "Xem Dashboard",
      "parentCode": null,
      "type": "SCREEN",
      "sortOrder": 0
    },
    {
      "code": "MANAGE_PROJECT",
      "displayName": "Quản lý Dự án",
      "parentCode": null,
      "type": "SCREEN",
      "sortOrder": 1
    },
    {
      "code": "PROJECT_CREATE",
      "displayName": "Tạo dự án",
      "parentCode": "MANAGE_PROJECT",
      "type": "ACTION",
      "sortOrder": 0
    },
    {
      "code": "PROJECT_EDIT",
      "displayName": "Sửa dự án",
      "parentCode": "MANAGE_PROJECT",
      "type": "ACTION",
      "sortOrder": 1
    },
    {
      "code": "PROJECT_DELETE",
      "displayName": "Xóa dự án",
      "parentCode": "MANAGE_PROJECT",
      "type": "ACTION",
      "sortOrder": 2
    },
    {
      "code": "MANAGE_USER",
      "displayName": "Quản lý Người dùng",
      "parentCode": null,
      "type": "SCREEN",
      "sortOrder": 2
    },
    {
      "code": "USER_CREATE",
      "displayName": "Tạo người dùng",
      "parentCode": "MANAGE_USER",
      "type": "ACTION",
      "sortOrder": 0
    },
    {
      "code": "USER_EDIT",
      "displayName": "Sửa người dùng",
      "parentCode": "MANAGE_USER",
      "type": "ACTION",
      "sortOrder": 1
    },
    {
      "code": "USER_DEACTIVATE",
      "displayName": "Vô hiệu hóa người dùng",
      "parentCode": "MANAGE_USER",
      "type": "ACTION",
      "sortOrder": 2
    },
    {
      "code": "MANAGE_ROLE",
      "displayName": "Quản lý Phân quyền",
      "parentCode": null,
      "type": "SCREEN",
      "sortOrder": 3
    },
    {
      "code": "ROLE_CREATE",
      "displayName": "Tạo role",
      "parentCode": "MANAGE_ROLE",
      "type": "ACTION",
      "sortOrder": 0
    },
    {
      "code": "ROLE_EDIT",
      "displayName": "Sửa role",
      "parentCode": "MANAGE_ROLE",
      "type": "ACTION",
      "sortOrder": 1
    },
    {
      "code": "ROLE_DEACTIVATE",
      "displayName": "Vô hiệu hóa role",
      "parentCode": "MANAGE_ROLE",
      "type": "ACTION",
      "sortOrder": 2
    },
    {
      "code": "ROLE_ASSIGN_PERMISSIONS",
      "displayName": "Gán quyền cho role",
      "parentCode": "MANAGE_ROLE",
      "type": "ACTION",
      "sortOrder": 3
    },
    {
      "code": "SYSTEM_SETTINGS",
      "displayName": "Cài đặt Hệ thống",
      "parentCode": null,
      "type": "SCREEN",
      "sortOrder": 4
    }
  ]
}
```

**Notes**:
- Endpoint này **không có** CRUD — Permission là dữ liệu tĩnh.
- FE build cây bằng cách: lấy tất cả items, nhóm theo `parentCode`. Root nodes = `parentCode: null`.
- `sortOrder` dùng để sắp xếp trong cùng cấp.
