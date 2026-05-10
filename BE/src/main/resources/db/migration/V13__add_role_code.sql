-- V13: Add role_code column to role table
ALTER TABLE role ADD COLUMN role_code VARCHAR(100);

-- Populate existing roles: uppercase role_name, replace spaces with _
UPDATE role SET role_code = UPPER(REPLACE(role_name, ' ', '_'));

-- Make it NOT NULL after populating
ALTER TABLE role ALTER COLUMN role_code SET NOT NULL;

-- Unique index (case-insensitive, non-deleted only)
CREATE UNIQUE INDEX idx_role_code_lower ON role (LOWER(role_code)) WHERE deleted = FALSE;
