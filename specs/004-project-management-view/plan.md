# Implementation Plan: Quản Lý Các Dự Án (Project Management View)

**Branch**: `004-project-management-view` | **Date**: 2026-05-07 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/004-project-management-view/spec.md`

---

## Summary

Build a "Quản Lý Các Dự Án" screen that displays active project monthly records for a selected month in an accordion/card layout. Each project card contains 6 expandable attribute groups; each group supports per-group inline editing with Save/Cancel. Formula fields display as disabled with a "Tự tính" badge. An overwrite warning fires at Save time when any changed field had a prior stored value.

The backend already provides all required read and update APIs from Feature 003. This feature adds:
- A new static field-metadata endpoint (BE) so FE knows which fields are formula vs manual
- A rewritten `ProjectManagementView.vue` (FE) implementing the accordion/card + per-group edit pattern
- A new `ProjectCard.vue` sub-component (FE) for the per-project card with 6 group sections

---

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x
**Primary Dependencies (BE)**: Spring Boot, Maven, PostgreSQL 15+, Lombok, SpringDoc OpenAPI
**Primary Dependencies (FE)**: Vue Router, Pinia, PrimeVue 4.3.x Aura, PrimeIcons
**Storage**: PostgreSQL — `project_monthly_record` table (from Feature 003 V3 migration)
**Testing (BE)**: JUnit 5 / Mockito (unit), Spring Boot Test + @Transactional (integration)
**Testing (FE)**: TypeScript compile check via `npx vue-tsc --noEmit` (no Vitest suite yet)
**Project Type**: web-service (BE) + web-app (FE)
**Performance Goals**: List loads in < 3 s; group expand is instant (client-side state)
**Constraints**:
  - BigDecimal for all financial fields (§4.3 constitution)
  - Formula fields read-only in FE; BE enforces via mapper (§9.2 constitution)
  - Cross-month save triggers user confirmation (§9.3 constitution)
  - All PrimeVue components must be locally imported (not globally registered)
  - Per-group update merges edited group fields into a full `ProjectMonthRecordRequest` to avoid null-clearing other groups
**Scale/Scope**: Internal tool; ~10–50 concurrent users; ~20 projects per month expected

---

## Constitution Check

*Reference: `.specify/memory/constitution.md`*

| § | Rule | Status | Notes |
|---|------|--------|-------|
| §4.3 | BigDecimal for financial fields | ✅ PASS | All existing entity/DTO fields already BigDecimal |
| §4.4 | Calculation logic has unit tests | ✅ PASS | `MonthlyCalculationServiceTest` (18 tests) from Feature 003 |
| §4.4 | Cross-month updates have integration tests | ✅ PASS | `ProjectMonthRecordServiceIntegrationTest` from Feature 003 |
| §5.3 | UI has Table, Form, Dialog, Toast, Loading, Empty, Error | ✅ PASS | All required per-group; designed below |
| §5.4 | Formula fields read-only with clear visual indicator | ✅ PASS | "Tự tính" badge + disabled input |
| §9.2 | Backend enforces formula field immutability | ✅ PASS | `applyRequest` mapper only copies manual fields |
| §9.3 | Overwrite warning before saving existing data | ✅ PASS | Per-group save fires warning if any group field had prior value |
| §10.3 | Cross-month actions require confirmation | ✅ PASS | Save warning covers this |

**No gate violations. All checks pass.**

---

## Project Structure

### Documentation (this feature)

```text
specs/004-project-management-view/
├── plan.md              ← This file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── field-metadata-api.md
│   └── existing-api-summary.md
└── tasks.md             ← Phase 2 output (/speckit.tasks)
```

### Source Code (modified/created by this feature)

```text
BE/
└── src/main/java/com/internal/projectmgmt/
    ├── controller/
    │   └── ProjectMonthRecordController.java   [MODIFY — add /field-metadata endpoint]
    ├── dto/monthlyrecord/
    │   └── FieldMetadataResponse.java          [CREATE]
    └── service/
        └── ProjectMonthRecordService.java       [MODIFY — add getFieldMetadata()]

FE/src/
├── views/
│   └── ProjectManagementView.vue               [REWRITE — accordion/card layout]
├── components/project-management/
│   ├── ProjectCard.vue                         [CREATE — per-project card with 6 groups]
│   └── MonthlyRecordDialog.vue                 [KEEP — still used for deep-link fallback]
├── types/
│   └── project-monthly-record.ts              [MODIFY — add FieldMetadata types]
└── services/
    └── projectMonthlyRecordService.ts          [MODIFY — add getFieldMetadata()]
```

---

## Complexity Tracking

No constitution violations requiring justification.

---

## Phase 0 — Research

### R1: Per-Group Update Strategy

**Question**: The existing `PUT /api/binance/project-monthly-records/{id}` uses `applyRequest()` which blindly copies ALL manual fields from the request body (null = clear). For per-group editing, sending only Group 2 fields would null-clear Groups 1, 3, 4, 5.

**Decision**: FE loads the full record detail (`getById`) when first entering any group's edit mode for a project. It maintains a `currentRecord` ref with all field values. When saving a group's changes, the FE builds a `ProjectMonthRecordRequest` by merging: (a) all current record values for non-edited groups, (b) the edited group's new form values. This prevents null-clearing and requires no new BE PATCH endpoints.

**Rationale**: Simpler than adding 6 per-group PATCH endpoints; the full record is already loaded for display; the merge is a pure FE operation with no new BE surface.

**Alternative rejected**: Per-group PATCH endpoints — would require BE changes to 6 new endpoints and 6 partial mapper methods, adding significant complexity for the same result.

---

### R2: Field Metadata — Static vs Dynamic

**Question**: Spec requires backend to supply metadata telling FE which fields are formula vs manual. The field classification is static (defined by business rules, not per-record), so it never changes at runtime.

**Decision**: Add a single `GET /api/binance/project-monthly-records/field-metadata` endpoint that returns a hard-coded `FieldMetadataResponse` (one object per group, each containing `manualFields: []` and `formulaFields: []` lists of field names). The FE calls this once on mount and caches the result locally.

**Rationale**: Satisfies the spec requirement. Since the data is static, no DB query is needed — the method simply returns a predefined constant. FE benefits from having an authoritative source rather than duplicating the classification in code.

**Alternative rejected**: FE hard-coding the field lists — would violate §2.2 (BE is source of truth for business rules). The field classification is a business rule.

---

### R3: Accordion/Card Layout with PrimeVue

**Decision**: Use PrimeVue `Panel` component (`:toggleable="true"`) for each project card, and nested PrimeVue `Fieldset` components (`:toggleable="true"`) for each of the 6 attribute groups inside. `Panel` renders a collapsible card; `Fieldset` renders a labeled collapsible section — together these implement the two-level accordion described in the spec.

**Rationale**: Both components are already available and used elsewhere in the codebase. `Fieldset` is already used in `MonthlyRecordDialog.vue` for the 6 groups. No custom accordion CSS needed.

---

### R4: PrimeVue Component Import Requirement

**Decision**: ALL PrimeVue components must be explicitly imported in each Vue SFC's `<script setup>`. Confirmed from previous session: PrimeVue 4.x Aura theme does not auto-register components globally; `main.ts` only registers `PrimeVue` config, `ToastService`, and `ConfirmationService`.

**Components needed in `ProjectManagementView.vue`**: `DatePicker`, `ProgressSpinner`, `Message`, `Toast`, `ConfirmDialog`
**Components needed in `ProjectCard.vue`**: `Panel`, `Fieldset`, `InputNumber`, `Button`, `Tag`, `ConfirmDialog`

---

### R5: Overwrite Warning — FE Implementation

**Decision**: Track a `originalGroupValues` snapshot per group when the user clicks Edit. On Save, compare form values against the snapshot: if any field changed from non-null to a different value, set `hasOverwrite = true`. If `hasOverwrite`, show `ConfirmDialog` with the exact message from spec before calling the API.

**Detail**: The `useConfirm()` composable is already available and used in `MonthlyRecordDialog.vue`. Reuse the same pattern.

---

## Phase 1 — Design

See [research.md](./research.md) for full research details.

---
