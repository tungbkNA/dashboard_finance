-- V14: API-based permissions — add granular API permissions, new screen modules, admin all-access

-- ============================================================
-- 1. New SCREEN permissions for missing modules
-- ============================================================
INSERT INTO permission (code, display_name, parent_code, type, sort_order) VALUES
    ('MANAGE_PROJECT_SETTINGS', 'Quản lý Cài đặt dự án',  NULL, 'SCREEN', 15),
    ('MANAGE_MONTHLY_RECORD',   'Quản lý Bản ghi tháng',  NULL, 'SCREEN', 25)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 2. New ACTION (API) permissions under each screen
-- ============================================================
INSERT INTO permission (code, display_name, parent_code, type, sort_order) VALUES
    -- Dashboard APIs
    ('DASHBOARD_VIEW',           'API: Xem dữ liệu Dashboard',       'VIEW_DASHBOARD',          'ACTION', 11),

    -- Project APIs
    ('PROJECT_VIEW',             'API: Xem danh sách dự án',          'MANAGE_PROJECT',          'ACTION', 20),
    ('PROJECT_IMPORT',           'API: Import dự án từ Excel',        'MANAGE_PROJECT',          'ACTION', 24),

    -- Project Settings APIs (customers, project-types)
    ('PROJECT_SETTINGS_VIEW',    'API: Xem cài đặt dự án',           'MANAGE_PROJECT_SETTINGS', 'ACTION', 16),
    ('PROJECT_SETTINGS_MANAGE',  'API: Thêm/sửa/xóa cài đặt dự án', 'MANAGE_PROJECT_SETTINGS', 'ACTION', 17),

    -- Monthly Record APIs
    ('MONTHLY_RECORD_VIEW',      'API: Xem bản ghi tháng',           'MANAGE_MONTHLY_RECORD',   'ACTION', 26),
    ('MONTHLY_RECORD_EDIT',      'API: Sửa bản ghi tháng',           'MANAGE_MONTHLY_RECORD',   'ACTION', 27),

    -- User APIs (USER_CREATE, USER_EDIT, USER_DEACTIVATE already exist)
    ('USER_VIEW',                'API: Xem danh sách người dùng',     'MANAGE_USER',             'ACTION', 30),

    -- Role APIs (ROLE_CREATE, ROLE_EDIT, ROLE_DEACTIVATE, ROLE_ASSIGN_PERMISSIONS already exist)
    ('ROLE_VIEW',                'API: Xem danh sách role',           'MANAGE_ROLE',             'ACTION', 40),
    ('PERMISSION_VIEW',          'API: Xem danh sách quyền',         'MANAGE_ROLE',             'ACTION', 45),

    -- Handbook APIs
    ('HANDBOOK_VIEW',            'API: Xem sổ tay trung tâm',        'MANAGE_HANDBOOK',         'ACTION', 51),
    ('HANDBOOK_MANAGE',          'API: Thêm/sửa/xóa sổ tay',        'MANAGE_HANDBOOK',         'ACTION', 52)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 3. Assign ALL permissions (old + new) to SYSTEM_ADMIN role
-- ============================================================
INSERT INTO role_permission (role_id, permission_code)
SELECT r.id, p.code
FROM role r, permission p
WHERE r.role_name = 'SYSTEM_ADMIN'
ON CONFLICT DO NOTHING;
