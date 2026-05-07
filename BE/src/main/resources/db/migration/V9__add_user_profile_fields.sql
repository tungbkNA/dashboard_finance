-- V9: Add phone, position, employee_code to app_user
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS phone          VARCHAR(10),
    ADD COLUMN IF NOT EXISTS position       VARCHAR(10),
    ADD COLUMN IF NOT EXISTS employee_code  VARCHAR(50);
