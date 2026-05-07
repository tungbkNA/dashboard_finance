-- V8: Add is_system flag to app_user
-- System accounts (admin) are excluded from UI lists and cannot be edited/deleted

ALTER TABLE app_user
    ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark existing admin as system account (idempotent — 0 rows if not yet seeded)
UPDATE app_user SET is_system = TRUE WHERE username = 'admin';
