-- V2: Feature 002 — Cài đặt dự án
-- Creates project_type, customer, project tables with soft delete

CREATE TYPE project_status AS ENUM ('OPEN', 'INPROGRESS', 'PENDING', 'DONE', 'CLOSE');
CREATE TYPE project_status_contract AS ENUM ('NO_CONTRACT', 'HAS_CONTRACT');

CREATE TABLE project_type (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    key            VARCHAR(50)  NOT NULL,
    value          VARCHAR(255) NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_project_type_key_lower
    ON project_type(LOWER(key))
    WHERE deleted = FALSE;

CREATE TABLE customer (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    customer_code  VARCHAR(50)  NOT NULL,
    customer_name  VARCHAR(255) NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_customer_code_lower
    ON customer(LOWER(customer_code))
    WHERE deleted = FALSE;

CREATE TABLE project (
    id               UUID                    NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_code     VARCHAR(50)             NOT NULL,
    project_name     VARCHAR(255)            NOT NULL,
    represent_id     UUID                    NULL,
    customer_id      UUID                    NOT NULL REFERENCES customer(id),
    project_type_id  UUID                    NOT NULL REFERENCES project_type(id),
    price            NUMERIC(19,4)           NOT NULL DEFAULT 0 CHECK (price >= 0),
    status_contract  project_status_contract NOT NULL,
    status_project   project_status          NOT NULL,
    month_start      VARCHAR(7)              NOT NULL,
    month_end        VARCHAR(7)              NOT NULL,
    deleted          BOOLEAN                 NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_project_code_lower
    ON project(LOWER(project_code))
    WHERE deleted = FALSE;
