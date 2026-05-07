# Data Model: Quản Lý Bản Ghi Dự Án Theo Tháng

**Feature**: 003-project-monthly-records  
**Date**: 2026-05-07

---

## Entities

### ProjectMonthRecord

**Table**: `project_monthly_record`  
**Description**: Bản ghi dữ liệu của một dự án trong một tháng cụ thể. Xác định duy nhất bởi `(project_id, month_key)`.

#### Identity & Metadata

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| `id` | UUID | NO | PK | Auto-generated UUID |
| `project_id` | UUID | NO | FK → project.id | Dự án cha |
| `month_key` | VARCHAR(7) | NO | format YYYY-MM | Tháng của bản ghi, ví dụ `2026-01` |
| `active` | BOOLEAN | NO | DEFAULT TRUE | FALSE khi tháng bị đưa ra ngoài khoảng hợp đồng |
| `created_at` | TIMESTAMPTZ | NO | SET on INSERT | Thời điểm tạo |
| `updated_at` | TIMESTAMPTZ | NO | SET on INSERT+UPDATE | Thời điểm cập nhật cuối |

**Unique constraint**: `UNIQUE (project_id, month_key)`  
**Index**: `(project_id, month_key)` cho query filter theo tháng; `(month_key)` cho filter toàn hệ thống theo tháng

---

#### Nhóm 1: Tồn đầu kỳ (Opening Stock)

*Tháng đầu tiên của dự án: tất cả nhập tay. Tháng sau: 5 trường auto-populate từ Tồn cuối kỳ tháng trước (không cho sửa); `g1_slsx_ton_tu_sx_hd` luôn nhập tay.*

| Column | Type | Nullable | Input | Description |
|--------|------|----------|-------|-------------|
| `g1_ra_ton` | DECIMAL(19,4) | YES | Manual (tháng đầu) / Auto (tháng sau ← g6_ra_ton) | RA tồn |
| `g1_slsx_ton_tu_sx_hd` | DECIMAL(19,4) | YES | Manual (mọi tháng) | SLSX tồn tự SX theo HĐ |
| `g1_slsx_ton_tu_sx_ht_hd` | DECIMAL(19,4) | YES | Manual (tháng đầu) / Auto (← g6_slsx_ton_ht) | SLSX tồn tự SX hoàn thiện theo HĐ |
| `g1_slsx_ton_tu_sx_dd_hd` | DECIMAL(19,4) | YES | Manual (tháng đầu) / Auto (← g6_slsx_ton_dd) | SLSX tồn tự SX dở dang theo HĐ |
| `g1_slsx_os_ton` | DECIMAL(19,4) | YES | Manual (tháng đầu) / Auto (← g6_slsx_os_ton) | SLSX OS tồn |
| `g1_slsx_os_ton_ht` | DECIMAL(19,4) | YES | Manual (tháng đầu) / Auto (← g6_slsx_os_ton_ht) | SLSX OS tồn hoàn thiện |

---

#### Nhóm 2: Kế hoạch tháng (Monthly Plan)

| Column | Type | Nullable | Input | Description |
|--------|------|----------|-------|-------------|
| `g2_headcount` | DECIMAL(19,4) | YES | Manual | Headcount tháng |
| `g2_ra` | DECIMAL(19,4) | YES | Manual | RA |
| `g2_slsx_tu_sx` | DECIMAL(19,4) | YES | Manual | SLSX Tự SX |
| `g2_slsx_os` | DECIMAL(19,4) | YES | Manual | SLSX OS |
| `g2_lien_ket` | DECIMAL(19,4) | YES | Manual | Liên kết |
| `g2_tong_slsx_du_kien` | DECIMAL(19,4) | YES | **Formula** (snapshot) | Tổng SLSX dự kiến = g2_slsx_tu_sx + g2_slsx_os + g2_lien_ket |
| `g2_slsx_tu_sx_ht_trong_thang` | DECIMAL(19,4) | YES | Manual | SLSX tự SX hoàn thiện trong tháng |
| `g2_slsx_tu_sx_dd` | DECIMAL(19,4) | YES | Manual | SLSX tự SX dở dang |
| `g2_slsx_os_ht` | DECIMAL(19,4) | YES | Manual | SLSX OS hoàn thiện |
| `g2_slsx_os_dd` | DECIMAL(19,4) | YES | Manual | SLSX OS dở dang |
| `g2_cpbqtb` | DECIMAL(19,4) | YES | Manual | CPBQTB tháng |
| `g2_ty_suat_lng` | DECIMAL(19,4) | YES | Manual | Tỷ suất LNG (%) |

---

#### Nhóm 3: Thực hiện SLSX đến NGÀY (Actual Production to Date)

| Column | Type | Nullable | Input | Description |
|--------|------|----------|-------|-------------|
| `g3_ra` | DECIMAL(19,4) | YES | Manual | RA |
| `g3_tong_slsx_hd` | DECIMAL(19,4) | YES | Manual | Tổng SLSX theo HĐ |
| `g3_ee` | DECIMAL(19,4) | YES | **Formula** (snapshot) | EE = g3_tong_slsx_hd / g3_ra × 100; NULL khi g3_ra = 0 hoặc NULL; round 2dp |
| `g3_slsx_tu_sx_ht` | DECIMAL(19,4) | YES | Manual | SLSX tự SX hoàn thiện |
| `g3_slsx_tu_sx_dd` | DECIMAL(19,4) | YES | Manual | SLSX tự SX dở dang |
| `g3_slsx_os_dd` | DECIMAL(19,4) | YES | Manual | SLSX OS dở dang |
| `g3_slsx_os_ton_ht` | DECIMAL(19,4) | YES | Manual | SLSX OS tồn hoàn thiện |

---

#### Nhóm 4: Kế hoạch doanh thu (Planned Revenue)

| Column | Type | Nullable | Input | Description |
|--------|------|----------|-------|-------------|
| `g4_tu_slsx_ton_ht` | DECIMAL(19,4) | YES | Manual | Tự SLSX Tồn hoàn thiện |
| `g4_tu_slsx_trong_thang` | DECIMAL(19,4) | YES | Manual | Tự SLSX trong tháng |
| `g4_slsx_os_ton` | DECIMAL(19,4) | YES | Manual | SLSX OS tồn |
| `g4_slsx_os_trong_thang` | DECIMAL(19,4) | YES | Manual | SLSX OS trong tháng |
| `g4_lk` | DECIMAL(19,4) | YES | Manual | LK |
| `g4_tong` | DECIMAL(19,4) | YES | **Formula** (snapshot) | Tổng = g4_tu_slsx_ton_ht + g4_tu_slsx_trong_thang + g4_slsx_os_ton + g4_slsx_os_trong_thang + g4_lk |
| `g4_doanh_thu` | DECIMAL(19,4) | YES | **Formula** (snapshot) | Doanh thu = g4_tong × project.price; round 0dp |
| `g4_ti_suat_lng_du_kien` | DECIMAL(19,4) | YES | Manual | Tỉ suất LNG dự kiến (%) |
| `g4_lng_du_kien` | DECIMAL(19,4) | YES | Manual | LNG dự kiến (VNĐ) |

---

#### Nhóm 5: Thực hiện nghiệm thu (Acceptance)

| Column | Type | Nullable | Input | Description |
|--------|------|----------|-------|-------------|
| `g5_ra_tuong_ung_slnt` | DECIMAL(19,4) | YES | Manual | RA tương ứng SLNT |
| `g5_nt_slsx_ton_ht` | DECIMAL(19,4) | YES | Manual | NT SLSX Tồn hoàn thiện |
| `g5_nt_slsx_trong_thang` | DECIMAL(19,4) | YES | Manual | NT SLSX trong tháng |
| `g5_nt_slsx_os_ton` | DECIMAL(19,4) | YES | Manual | NT SLSX OS tồn |
| `g5_nt_slsx_os_trong_thang` | DECIMAL(19,4) | YES | Manual | NT SLSX OS trong tháng |
| `g5_tong_slnt` | DECIMAL(19,4) | YES | **Formula** (snapshot) | Tổng SLNT = g5_nt_slsx_ton_ht + g5_nt_slsx_trong_thang + g5_nt_slsx_os_ton + g5_nt_slsx_os_trong_thang |
| `g5_doanh_thu` | DECIMAL(19,4) | YES | **Formula** (snapshot) | Doanh thu = g5_tong_slnt × project.price; round 0dp |
| `g5_ti_suat_lng` | DECIMAL(19,4) | YES | Manual | Tỉ suất LNG (%) |
| `g5_lng_vnd` | DECIMAL(19,4) | YES | Manual | LNG (VNĐ) |

---

#### Nhóm 6: Tồn cuối kỳ (Closing Stock) — toàn bộ công thức

| Column | Type | Nullable | Input | Formula |
|--------|------|----------|-------|---------|
| `g6_ra_ton` | DECIMAL(19,4) | YES | **Formula** (snapshot) | g1_ra_ton + g3_ra − g5_ra_tuong_ung_slnt |
| `g6_slsx_ton_ht` | DECIMAL(19,4) | YES | **Formula** (snapshot) | g1_slsx_ton_tu_sx_ht_hd + g3_slsx_tu_sx_ht − g5_nt_slsx_ton_ht − g5_nt_slsx_os_trong_thang |
| `g6_slsx_ton_dd` | DECIMAL(19,4) | YES | **Formula** (snapshot) | g3_slsx_tu_sx_dd + g1_slsx_ton_tu_sx_dd_hd |
| `g6_slsx_os_ton` | DECIMAL(19,4) | YES | **Formula** (snapshot) | g1_slsx_os_ton + g3_slsx_os_dd |
| `g6_slsx_os_ton_ht` | DECIMAL(19,4) | YES | **Formula** (snapshot) | g1_slsx_os_ton_ht + g3_slsx_os_ton_ht − g5_nt_slsx_os_ton − g5_nt_slsx_os_trong_thang |

---

## Relationships

```
Project (1) ──────── (N) ProjectMonthRecord
  id ←──────────────── project_id
  price ────────────── used in g4_doanh_thu, g5_doanh_thu formulas
  month_start ─────── determines first month (g1 fields manually entered)
  month_end ───────── upper bound for record generation
```

## State Transitions

```
ProjectMonthRecord.active:

  [tạo] → active = TRUE
  
  [project.monthEnd rút ngắn, tháng này ngoài khoảng mới]
    → user confirms warning
    → active = FALSE  (không hiện trong danh sách)
    
  [project.monthEnd mở rộng lại, bao gồm tháng này]
    → active = TRUE  (reactivate thay vì tạo mới)
```

## Formula Evaluation Rules

| Trường | null input được xử lý như | Kết quả khi tất cả input null |
|--------|--------------------------|-------------------------------|
| Tổng SLSX dự kiến (g2) | 0 | 0 |
| EE (g3_ee) | RA = null/0 → trả NULL | NULL |
| g4_tong, g4_doanh_thu | 0 | 0 |
| g5_tong_slnt, g5_doanh_thu | 0 | 0 |
| Tất cả g6_* | 0 | 0 |

## Validation Rules

| Rule | Description |
|------|-------------|
| month_key format | `^\d{4}-(0[1-9]\|1[0-2])$` |
| project_id + month_key | UNIQUE trong DB |
| Tồn đầu kỳ tháng N>1 | g1_ra_ton, g1_slsx_ton_tu_sx_ht_hd, g1_slsx_ton_tu_sx_dd_hd, g1_slsx_os_ton, g1_slsx_os_ton_ht không cho user cập nhật — chỉ cascade từ g6 tháng trước |
| Trường công thức | Không nhận giá trị từ API request — bị ignore hoặc báo lỗi 400 nếu client cố gửi |
