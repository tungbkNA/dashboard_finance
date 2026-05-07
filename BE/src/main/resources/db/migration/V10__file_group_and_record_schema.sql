-- V10: Create file_group and file_record tables for Central Handbook module

CREATE TABLE file_group (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_file_group_name UNIQUE (name)
);

CREATE TABLE file_record (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name       VARCHAR(200) NOT NULL,
    file_url        VARCHAR(2048) NOT NULL,
    file_group_id   UUID NOT NULL,
    created_by      VARCHAR(50) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_file_record_group FOREIGN KEY (file_group_id)
        REFERENCES file_group(id) ON DELETE RESTRICT
);

CREATE INDEX idx_file_record_group ON file_record(file_group_id);
CREATE INDEX idx_file_record_name  ON file_record(file_name);
