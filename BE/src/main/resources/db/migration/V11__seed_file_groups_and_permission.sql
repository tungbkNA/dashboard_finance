-- V11: Seed default file groups and MANAGE_HANDBOOK permission

INSERT INTO file_group (id, name, description, active) VALUES
    (gen_random_uuid(), 'Kế hoạch tháng', 'Tài liệu kế hoạch hàng tháng', true),
    (gen_random_uuid(), 'Báo cáo', 'Các báo cáo tổng hợp', true),
    (gen_random_uuid(), 'Quy trình', 'Tài liệu quy trình nội bộ', true);

-- Add MANAGE_HANDBOOK permission
INSERT INTO permission (code, display_name, type, sort_order)
VALUES ('MANAGE_HANDBOOK', 'Quản lý Sổ tay trung tâm', 'SCREEN', 0)
ON CONFLICT (code) DO NOTHING;

-- Assign to admin role (SYSTEM_ADMIN)
INSERT INTO role_permission (role_id, permission_code)
SELECT r.id, 'MANAGE_HANDBOOK'
FROM role r
WHERE r.role_name = 'SYSTEM_ADMIN'
ON CONFLICT DO NOTHING;
