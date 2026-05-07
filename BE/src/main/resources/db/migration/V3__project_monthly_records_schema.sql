-- V3: Feature 003 — Quản lý bản ghi dự án theo tháng
-- Creates project_monthly_record table

CREATE TABLE project_monthly_record (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_id      UUID            NOT NULL REFERENCES project(id),
    month_key       VARCHAR(7)      NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Nhóm 1: Tồn đầu kỳ
    g1_ra_ton                   DECIMAL(19,4),
    g1_slsx_ton_tu_sx_hd        DECIMAL(19,4),
    g1_slsx_ton_tu_sx_ht_hd     DECIMAL(19,4),
    g1_slsx_ton_tu_sx_dd_hd     DECIMAL(19,4),
    g1_slsx_os_ton              DECIMAL(19,4),
    g1_slsx_os_ton_ht           DECIMAL(19,4),

    -- Nhóm 2: Kế hoạch tháng
    g2_headcount                DECIMAL(19,4),
    g2_ra                       DECIMAL(19,4),
    g2_slsx_tu_sx               DECIMAL(19,4),
    g2_slsx_os                  DECIMAL(19,4),
    g2_lien_ket                 DECIMAL(19,4),
    g2_tong_slsx_du_kien        DECIMAL(19,4),  -- formula snapshot
    g2_slsx_tu_sx_ht_trong_thang DECIMAL(19,4),
    g2_slsx_tu_sx_dd            DECIMAL(19,4),
    g2_slsx_os_ht               DECIMAL(19,4),
    g2_slsx_os_dd               DECIMAL(19,4),
    g2_cpbqtb                   DECIMAL(19,4),
    g2_ty_suat_lng              DECIMAL(19,4),

    -- Nhóm 3: Thực hiện SLSX đến NGÀY
    g3_ra                       DECIMAL(19,4),
    g3_tong_slsx_hd             DECIMAL(19,4),
    g3_ee                       DECIMAL(19,4),  -- formula snapshot
    g3_slsx_tu_sx_ht            DECIMAL(19,4),
    g3_slsx_tu_sx_dd            DECIMAL(19,4),
    g3_slsx_os_dd               DECIMAL(19,4),
    g3_slsx_os_ton_ht           DECIMAL(19,4),

    -- Nhóm 4: Kế hoạch doanh thu
    g4_tu_slsx_ton_ht           DECIMAL(19,4),
    g4_tu_slsx_trong_thang      DECIMAL(19,4),
    g4_slsx_os_ton              DECIMAL(19,4),
    g4_slsx_os_trong_thang      DECIMAL(19,4),
    g4_lk                       DECIMAL(19,4),
    g4_tong                     DECIMAL(19,4),  -- formula snapshot
    g4_doanh_thu                DECIMAL(19,4),  -- formula snapshot
    g4_ti_suat_lng_du_kien      DECIMAL(19,4),
    g4_lng_du_kien              DECIMAL(19,4),

    -- Nhóm 5: Thực hiện nghiệm thu
    g5_ra_tuong_ung_slnt        DECIMAL(19,4),
    g5_nt_slsx_ton_ht           DECIMAL(19,4),
    g5_nt_slsx_trong_thang      DECIMAL(19,4),
    g5_nt_slsx_os_ton           DECIMAL(19,4),
    g5_nt_slsx_os_trong_thang   DECIMAL(19,4),
    g5_tong_slnt                DECIMAL(19,4),  -- formula snapshot
    g5_doanh_thu                DECIMAL(19,4),  -- formula snapshot
    g5_ti_suat_lng              DECIMAL(19,4),
    g5_lng_vnd                  DECIMAL(19,4),

    -- Nhóm 6: Tồn cuối kỳ (all formula snapshots)
    g6_ra_ton                   DECIMAL(19,4),
    g6_slsx_ton_ht              DECIMAL(19,4),
    g6_slsx_ton_dd              DECIMAL(19,4),
    g6_slsx_os_ton              DECIMAL(19,4),
    g6_slsx_os_ton_ht           DECIMAL(19,4),

    CONSTRAINT uq_project_month UNIQUE (project_id, month_key)
);

CREATE INDEX idx_pmr_project_month ON project_monthly_record (project_id, month_key);
CREATE INDEX idx_pmr_month_key    ON project_monthly_record (month_key);
