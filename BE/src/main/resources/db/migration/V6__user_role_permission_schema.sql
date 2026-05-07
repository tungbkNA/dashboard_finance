-- V6: User, Role, Permission schema + FK on project.represent_id
-- Follows research.md R-008 (app_user name), R-009 (represent_id migration)

-- ============================================================
-- 1. permission table (static lookup data, STRING PK)
-- ============================================================
CREATE TABLE permission (
    code         VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    parent_code  VARCHAR(100) NULL REFERENCES permission(code),
    type         VARCHAR(20)  NOT NULL CHECK (type IN ('SCREEN', 'ACTION')),
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT pk_permission PRIMARY KEY (code)
);

-- ============================================================
-- 2. role table
-- ============================================================
CREATE TABLE role (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    role_name   VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_role_name_lower ON role (LOWER(role_name)) WHERE deleted = FALSE;

-- ============================================================
-- 3. app_user table (not 'user' — reserved word in PostgreSQL)
-- ============================================================
CREATE TABLE app_user (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id       UUID         NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_app_user     PRIMARY KEY (id),
    CONSTRAINT fk_app_user_role FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE UNIQUE INDEX idx_app_user_username ON app_user (username);
CREATE UNIQUE INDEX idx_app_user_email    ON app_user (email);
CREATE INDEX        idx_app_user_role_id  ON app_user (role_id);

-- ============================================================
-- 4. role_permission junction table
-- ============================================================
CREATE TABLE role_permission (
    role_id         UUID         NOT NULL REFERENCES role(id),
    permission_code VARCHAR(100) NOT NULL REFERENCES permission(code),
    CONSTRAINT pk_role_permission PRIMARY KEY (role_id, permission_code)
);

-- ============================================================
-- 5. Update project.represent_id: set orphan values to NULL,
--    then add FK constraint (ON DELETE SET NULL)
-- ============================================================
UPDATE project
SET represent_id = NULL
WHERE represent_id IS NOT NULL
  AND represent_id NOT IN (SELECT id FROM app_user);

ALTER TABLE project
    ADD CONSTRAINT fk_project_represent_user
    FOREIGN KEY (represent_id)
    REFERENCES app_user(id)
    ON DELETE SET NULL;
