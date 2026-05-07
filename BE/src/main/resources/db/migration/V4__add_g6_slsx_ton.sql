-- Add "SLSX tồn" to G6 Tồn cuối kỳ.
-- Formula: g6_slsx_ton = g6_slsx_ton_ht + g6_slsx_ton_dd + g6_slsx_os_ton + g6_slsx_os_ton_ht
-- Cascade: g6_slsx_ton of month n → g1_slsx_ton_tu_sx_hd of month n+1

ALTER TABLE project_monthly_record
    ADD COLUMN g6_slsx_ton DECIMAL(19, 4);
