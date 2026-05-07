-- Feature 005: Cross-Month Recalculation
-- Adds `locked` column to project_monthly_record and creates field_change_audit_log table

ALTER TABLE project_monthly_record
    ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE field_change_audit_log (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id            UUID        NOT NULL,
    month_key             VARCHAR(7)  NOT NULL,
    field_name            VARCHAR(100) NOT NULL,
    old_value             TEXT,
    new_value             TEXT,
    triggered_by_month_key VARCHAR(7) NOT NULL,
    event_id              VARCHAR(36) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fcal_event_id      ON field_change_audit_log(event_id);
CREATE INDEX idx_fcal_project_month ON field_change_audit_log(project_id, month_key);
CREATE INDEX idx_fcal_triggered_by  ON field_change_audit_log(triggered_by_month_key);
