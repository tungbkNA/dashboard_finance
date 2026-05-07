-- V12: Fix MANAGE_HANDBOOK permission that was missed due to V11 column mismatch

INSERT INTO permission (code, display_name, type, sort_order)
VALUES ('MANAGE_HANDBOOK', 'Quản lý Sổ tay trung tâm', 'SCREEN', 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permission (role_id, permission_code)
SELECT r.id, 'MANAGE_HANDBOOK'
FROM role r
WHERE r.role_name = 'SYSTEM_ADMIN'
ON CONFLICT DO NOTHING;
