-- V7: Seed 15 permissions + SYSTEM_ADMIN role with all permissions
-- Follows research.md R-010 and data-model.md permission table

-- ============================================================
-- 1. Seed permissions (5 SCREEN + 10 ACTION)
-- ============================================================
INSERT INTO permission (code, display_name, parent_code, type, sort_order) VALUES
    ('VIEW_DASHBOARD',          'Xem Dashboard',                  NULL,             'SCREEN', 10),
    ('MANAGE_PROJECT',          'Quản lý Dự án',                  NULL,             'SCREEN', 20),
    ('MANAGE_USER',             'Quản lý Người dùng',             NULL,             'SCREEN', 30),
    ('MANAGE_ROLE',             'Quản lý Phân quyền',             NULL,             'SCREEN', 40),
    ('SYSTEM_SETTINGS',         'Cài đặt Hệ thống',               NULL,             'SCREEN', 50),
    ('PROJECT_CREATE',          'Tạo dự án',                      'MANAGE_PROJECT', 'ACTION', 21),
    ('PROJECT_EDIT',            'Sửa dự án',                      'MANAGE_PROJECT', 'ACTION', 22),
    ('PROJECT_DELETE',          'Xóa dự án',                      'MANAGE_PROJECT', 'ACTION', 23),
    ('USER_CREATE',             'Tạo người dùng',                 'MANAGE_USER',    'ACTION', 31),
    ('USER_EDIT',               'Sửa người dùng',                 'MANAGE_USER',    'ACTION', 32),
    ('USER_DEACTIVATE',         'Vô hiệu hóa người dùng',         'MANAGE_USER',    'ACTION', 33),
    ('ROLE_CREATE',             'Tạo role',                       'MANAGE_ROLE',    'ACTION', 41),
    ('ROLE_EDIT',               'Sửa role',                       'MANAGE_ROLE',    'ACTION', 42),
    ('ROLE_DEACTIVATE',         'Vô hiệu hóa role',               'MANAGE_ROLE',    'ACTION', 43),
    ('ROLE_ASSIGN_PERMISSIONS', 'Gán quyền cho role',             'MANAGE_ROLE',    'ACTION', 44);

-- ============================================================
-- 2. Seed SYSTEM_ADMIN role
-- ============================================================
INSERT INTO role (id, role_name, description, active, deleted)
VALUES (gen_random_uuid(), 'SYSTEM_ADMIN', 'Quản trị viên hệ thống — toàn quyền', TRUE, FALSE);

-- ============================================================
-- 3. Assign all permissions to SYSTEM_ADMIN
-- ============================================================
INSERT INTO role_permission (role_id, permission_code)
SELECT r.id, p.code
FROM role r, permission p
WHERE r.role_name = 'SYSTEM_ADMIN';
