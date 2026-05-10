-- Remove SYSTEM_SETTINGS permission (Cấu hình screen removed from UI)
DELETE FROM role_permission WHERE permission_code = 'SYSTEM_SETTINGS';
DELETE FROM permission WHERE code = 'SYSTEM_SETTINGS';
