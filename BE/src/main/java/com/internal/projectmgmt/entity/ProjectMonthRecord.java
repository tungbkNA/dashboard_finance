package com.internal.projectmgmt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_monthly_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMonthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "month_key", nullable = false, length = 7)
    private String monthKey;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ---- Nhóm 1: Tồn đầu kỳ ----
    @Column(name = "g1_ra_ton", precision = 19, scale = 4)
    private BigDecimal g1RaTon;

    @Column(name = "g1_slsx_ton_tu_sx_hd", precision = 19, scale = 4)
    private BigDecimal g1SlsxTonTuSxHd;

    @Column(name = "g1_slsx_ton_tu_sx_ht_hd", precision = 19, scale = 4)
    private BigDecimal g1SlsxTonTuSxHtHd;

    @Column(name = "g1_slsx_ton_tu_sx_dd_hd", precision = 19, scale = 4)
    private BigDecimal g1SlsxTonTuSxDdHd;

    @Column(name = "g1_slsx_os_ton", precision = 19, scale = 4)
    private BigDecimal g1SlsxOsTon;

    @Column(name = "g1_slsx_os_ton_ht", precision = 19, scale = 4)
    private BigDecimal g1SlsxOsTonHt;

    // ---- Nhóm 2: Kế hoạch tháng ----
    @Column(name = "g2_headcount", precision = 19, scale = 4)
    private BigDecimal g2Headcount;

    @Column(name = "g2_ra", precision = 19, scale = 4)
    private BigDecimal g2Ra;

    @Column(name = "g2_slsx_tu_sx", precision = 19, scale = 4)
    private BigDecimal g2SlsxTuSx;

    @Column(name = "g2_slsx_os", precision = 19, scale = 4)
    private BigDecimal g2SlsxOs;

    @Column(name = "g2_lien_ket", precision = 19, scale = 4)
    private BigDecimal g2LienKet;

    @Column(name = "g2_tong_slsx_du_kien", precision = 19, scale = 4)
    private BigDecimal g2TongSlsxDuKien; // formula snapshot

    @Column(name = "g2_slsx_tu_sx_ht_trong_thang", precision = 19, scale = 4)
    private BigDecimal g2SlsxTuSxHtTrongThang;

    @Column(name = "g2_slsx_tu_sx_dd", precision = 19, scale = 4)
    private BigDecimal g2SlsxTuSxDd;

    @Column(name = "g2_slsx_os_ht", precision = 19, scale = 4)
    private BigDecimal g2SlsxOsHt;

    @Column(name = "g2_slsx_os_dd", precision = 19, scale = 4)
    private BigDecimal g2SlsxOsDd;

    @Column(name = "g2_cpbqtb", precision = 19, scale = 4)
    private BigDecimal g2Cpbqtb;

    @Column(name = "g2_ty_suat_lng", precision = 19, scale = 4)
    private BigDecimal g2TySuatLng;

    // ---- Nhóm 3: Thực hiện SLSX đến NGÀY ----
    @Column(name = "g3_ra", precision = 19, scale = 4)
    private BigDecimal g3Ra;

    @Column(name = "g3_tong_slsx_hd", precision = 19, scale = 4)
    private BigDecimal g3TongSlsxHd;

    @Column(name = "g3_ee", precision = 19, scale = 4)
    private BigDecimal g3Ee; // formula snapshot

    @Column(name = "g3_slsx_tu_sx_ht", precision = 19, scale = 4)
    private BigDecimal g3SlsxTuSxHt;

    @Column(name = "g3_slsx_tu_sx_dd", precision = 19, scale = 4)
    private BigDecimal g3SlsxTuSxDd;

    @Column(name = "g3_slsx_os_dd", precision = 19, scale = 4)
    private BigDecimal g3SlsxOsDd;

    @Column(name = "g3_slsx_os_ton_ht", precision = 19, scale = 4)
    private BigDecimal g3SlsxOsTonHt;

    // ---- Nhóm 4: Kế hoạch doanh thu ----
    @Column(name = "g4_tu_slsx_ton_ht", precision = 19, scale = 4)
    private BigDecimal g4TuSlsxTonHt;

    @Column(name = "g4_tu_slsx_trong_thang", precision = 19, scale = 4)
    private BigDecimal g4TuSlsxTrongThang;

    @Column(name = "g4_slsx_os_ton", precision = 19, scale = 4)
    private BigDecimal g4SlsxOsTon;

    @Column(name = "g4_slsx_os_trong_thang", precision = 19, scale = 4)
    private BigDecimal g4SlsxOsTrongThang;

    @Column(name = "g4_lk", precision = 19, scale = 4)
    private BigDecimal g4Lk;

    @Column(name = "g4_tong", precision = 19, scale = 4)
    private BigDecimal g4Tong; // formula snapshot

    @Column(name = "g4_doanh_thu", precision = 19, scale = 4)
    private BigDecimal g4DoanhThu; // formula snapshot

    @Column(name = "g4_ti_suat_lng_du_kien", precision = 19, scale = 4)
    private BigDecimal g4TiSuatLngDuKien;

    @Column(name = "g4_lng_du_kien", precision = 19, scale = 4)
    private BigDecimal g4LngDuKien;

    // ---- Nhóm 5: Thực hiện nghiệm thu ----
    @Column(name = "g5_ra_tuong_ung_slnt", precision = 19, scale = 4)
    private BigDecimal g5RaTuongUngSlnt;

    @Column(name = "g5_nt_slsx_ton_ht", precision = 19, scale = 4)
    private BigDecimal g5NtSlsxTonHt;

    @Column(name = "g5_nt_slsx_trong_thang", precision = 19, scale = 4)
    private BigDecimal g5NtSlsxTrongThang;

    @Column(name = "g5_nt_slsx_os_ton", precision = 19, scale = 4)
    private BigDecimal g5NtSlsxOsTon;

    @Column(name = "g5_nt_slsx_os_trong_thang", precision = 19, scale = 4)
    private BigDecimal g5NtSlsxOsTrongThang;

    @Column(name = "g5_tong_slnt", precision = 19, scale = 4)
    private BigDecimal g5TongSlnt; // formula snapshot

    @Column(name = "g5_doanh_thu", precision = 19, scale = 4)
    private BigDecimal g5DoanhThu; // formula snapshot

    @Column(name = "g5_ti_suat_lng", precision = 19, scale = 4)
    private BigDecimal g5TiSuatLng;

    @Column(name = "g5_lng_vnd", precision = 19, scale = 4)
    private BigDecimal g5LngVnd;

    // ---- Nhóm 6: Tồn cuối kỳ (all formula snapshots) ----
    @Column(name = "g6_ra_ton", precision = 19, scale = 4)
    private BigDecimal g6RaTon;

    @Column(name = "g6_slsx_ton_ht", precision = 19, scale = 4)
    private BigDecimal g6SlsxTonHt;

    @Column(name = "g6_slsx_ton_dd", precision = 19, scale = 4)
    private BigDecimal g6SlsxTonDd;

    @Column(name = "g6_slsx_os_ton", precision = 19, scale = 4)
    private BigDecimal g6SlsxOsTon;

    @Column(name = "g6_slsx_os_ton_ht", precision = 19, scale = 4)
    private BigDecimal g6SlsxOsTonHt;

    @Column(name = "g6_slsx_ton", precision = 19, scale = 4)
    private BigDecimal g6SlsxTon; // formula snapshot: g6SlsxTonHt + g6SlsxTonDd + g6SlsxOsTon + g6SlsxOsTonHt

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
