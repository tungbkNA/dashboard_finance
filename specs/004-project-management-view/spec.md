# Feature Specification: Quản Lý Các Dự Án (Project Management View)

**Feature Branch**: `004-project-management-view`
**Created**: 2026-05-07
**Status**: Draft
**Input**: User description: "Xây dựng màn hình Quản Lý Các Dự Án để xem và nhập dữ liệu dự án theo tháng."

---

## Clarifications

### Session 2026-05-07

- Q: Khi bấm Edit, màn hình chỉnh sửa hiển thị theo cách nào — inline trong dòng, mở modal, hay edit theo từng nhóm riêng lẻ? → A: Inline in-place editing within each expanded group on the project row; Save/Cancel button pair appears on the row (no modal/dialog).
- Q: Cảnh báo "Thay đổi này có thể..." xuất hiện vào thời điểm nào trong quy trình chỉnh sửa? → A: Warning fires once when the user clicks Save, if one or more of the changed fields had prior stored values.
- Q: Layout tổng thể của danh sách dự án là gì — accordion lồng nhau, bảng ngang, hay master-detail? → A: Accordion/card per project; the 6 attribute groups are nested expandable sections inside each project card.
- Q: Có phân quyền xem/chỉnh sửa theo vai trò người dùng không? → A: No authorization; any user who can access the screen can both view and edit all records.
- Q: Khi lưu thất bại do lỗi mạng/server, giao diện xử lý như thế nào — giữ trạng thái edit hay hủy? → A: Display an error toast notification; keep the row in edit mode with unsaved inputs intact so the user can retry.
- Q: Edit theo từng dòng dự án hay từng nhóm thuộc tính? → A: Edit per attribute group — each expanded group has its own Edit button; clicking it puts only that group's fields into inline edit mode with Save/Cancel within the group.
- Q: Có cần lưu nháp trước khi xác nhận không? → A: No draft save needed at this stage; the user edits and saves directly per group.
- Q: Khi user hủy edit thì dữ liệu UI quay về trạng thái nào? → A: Cancel reverts the group's fields to the values they had before the user started editing that group (pre-edit state).
- Q: Trường công thức nên hiển thị icon hoặc label read-only như thế nào? → A: Formula fields display as disabled inputs with a "Tự tính" label (tag/badge) to clearly indicate the value is auto-calculated.
- Q: Khi tháng hiện tại không có dữ liệu thì màn hình hiển thị empty state ra sao? → A: Empty state displays the message "Không có dữ liệu dự án cho tháng này".
- Q: Filter tháng dùng mm/yyyy hay month picker? → A: Month picker UI control, displaying the selected month in mm/yyyy format.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Project Monthly Data (Priority: P1)

A data analyst opens the "Quản Lý Các Dự Án" screen to review all projects' performance data for the current month. They see a list of projects, each with its 6 attribute groups collapsed by default, giving a clean overview of all projects at once. They can change the month filter to look at data from past or future months.

**Why this priority**: Reading data is the most fundamental capability. Every other story depends on being able to navigate to the right month and see the right projects.

**Independent Test**: Can be fully tested by navigating to the screen, verifying the month filter defaults to the current month, and confirming all projects that have records for that month appear in the list.

**Acceptance Scenarios**:

1. **Given** the user opens the screen, **When** no month is selected, **Then** the screen defaults to the current month and displays all projects with monthly records for that month.
2. **Given** the screen is displaying data, **When** the user selects a different month using the month picker, **Then** the list refreshes to show only projects with records for the selected month.
3. **Given** a project has no record for the selected month, **When** the user views the list, **Then** that project does not appear in the list.
4. **Given** no projects have records for the selected month, **When** the user views the screen, **Then** the empty-state message "Không có dữ liệu dự án cho tháng này" is displayed.

---

### User Story 2 - Expand/Collapse Attribute Groups (Priority: P2)

A data analyst wants to drill into specific metric groups for a project without being overwhelmed by all fields at once. They click on a group header (e.g., "Kế hoạch tháng") to expand it and see the individual field values. Clicking again collapses it. Multiple groups can be open simultaneously, and the open/closed state does not affect stored data.

**Why this priority**: Without expand/collapse, the screen is too dense to be usable. This story unlocks usability for all subsequent inspection and editing tasks.

**Independent Test**: Can be fully tested by expanding and collapsing each of the 6 groups on a record, verifying field values appear/disappear, and confirming that expanding/collapsing causes no data changes.

**Acceptance Scenarios**:

1. **Given** a project record is displayed, **When** the user clicks a collapsed group header, **Then** the group expands to show all field values in that group.
2. **Given** a group is expanded, **When** the user clicks the group header again, **Then** the group collapses and field values are hidden.
3. **Given** two groups are expanded, **When** the user collapses one, **Then** the other remains expanded.
4. **Given** the user refreshes the page, **When** the page reloads, **Then** all groups return to their default collapsed state (expand/collapse state is not persisted).
5. **Given** a group is expanded, **When** the user changes the month filter, **Then** groups reset to collapsed after the list refreshes.

---

### User Story 3 - Edit Manual-Entry Fields (Priority: P3)

A data entry operator needs to update the manual-entry values for a specific attribute group within a project's monthly record. They expand the group, click its Edit button, enter new values into editable fields, and click Save within the group. Formula-derived fields are clearly marked with a "Tự tính" label, are displayed as disabled, and cannot be modified. After saving, a success notification confirms the change. Clicking Cancel reverts the group's fields to their pre-edit values.

**Why this priority**: Editing is the primary write operation. It must protect formula fields while enabling smooth data entry.

**Independent Test**: Can be fully tested by opening a record for editing, attempting to modify both manual and formula fields, and verifying only manual fields accept input, with a success toast appearing on save.

**Acceptance Scenarios**:

1. **Given** a record is in edit mode, **When** the user views a formula field, **Then** the field is visually marked as read-only and cannot be modified.
2. **Given** a record is in edit mode, **When** the user enters a value in a manual-entry field, **Then** the value is accepted for saving.
3. **Given** the user saves changes, **When** the save operation succeeds, **Then** a success toast notification is displayed and the group reflects the updated values.
4. **Given** a formula field has a stored value in the database, **When** the record is displayed, **Then** the stored value is shown (not recalculated) with a "Tự tính" label.
5. **Given** a formula field has no stored value, **When** the record is displayed, **Then** the system displays the calculated value with a "Tự tính" label.
6. **Given** a save is attempted with an invalid value (e.g., non-numeric in a numeric field), **When** the user submits, **Then** a validation error is shown next to the offending field and no data is saved.
7. **Given** the user clicks Cancel on a group in edit mode, **When** Cancel is confirmed, **Then** all fields in that group revert to the values they had before editing began.

---

### User Story 4 - Overwrite Warning for Existing Data (Priority: P3)

A data entry operator tries to edit a manual-entry field that already contains data. Because changing this value can affect other months' calculated data (cascade effects), the system warns them before allowing the change. The operator must explicitly confirm before the value is accepted. If they decline, the original value is restored.

**Why this priority**: Same priority as editing because this guard is part of the edit flow and prevents accidental data corruption.

**Independent Test**: Can be fully tested by modifying a manual field with an existing value, verifying the warning dialog appears, confirming the operator can proceed or cancel, and checking data is only changed on confirmation.

**Acceptance Scenarios**:

1. **Given** one or more changed manual-entry fields have a prior saved value, **When** the user clicks Save, **Then** a warning dialog appears: "Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác".
2. **Given** the warning dialog is shown, **When** the user confirms, **Then** all changed values are saved and the record is updated.
3. **Given** the warning dialog is shown, **When** the user cancels, **Then** no changes are saved and the group's fields revert to their pre-edit values.
4. **Given** all changed manual-entry fields were previously empty, **When** the user clicks Save, **Then** no warning is shown and values are saved directly.

---

### Edge Cases

- **Network error during save**: Resolved — error toast is shown, row stays in edit mode, inputs are preserved for retry (see FR-PM-020a).
- How does the system handle a project whose month range no longer includes the currently filtered month? The record is inactive and will not appear in the list (active filter applied by backend).
- What happens if two users edit the same record simultaneously? Out of scope — last-write wins.
- How does the system behave when a formula field’s dependencies are all null/zero? Backend returns null or zero as applicable; the field is displayed as empty or zero.
- What happens if the user edits multiple fields, some with existing values and some empty? The single save-time warning fires if any of the changed fields had a prior value (see FR-PM-015).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-PM-001**: The screen MUST be named "Quản Lý Các Dự Án" and accessible from the main navigation.
- **FR-PM-002**: The screen MUST default to filtering by the current calendar month when first opened.
- **FR-PM-003**: The screen MUST provide a month picker control allowing the user to select any month to filter records. The selected month MUST be displayed in `mm/yyyy` format within the picker.
- **FR-PM-004**: The screen MUST display only projects that have an active monthly record corresponding to the currently selected month. When no such records exist, the screen MUST display the empty-state message "Không có dữ liệu dự án cho tháng này".
- **FR-PM-004a**: The screen layout MUST be an accordion/card list: each project occupies one card row showing the project name and key summary values; clicking the card expands it to reveal the 6 nested attribute group sections.
- **FR-PM-005**: Each project entry MUST display the following 6 attribute groups: (1) Tồn đầu kỳ, (2) Kế hoạch tháng, (3) Thực hiện SLSX đến NGÀY, (4) Kế hoạch doanh thu, (5) Thực hiện nghiệm thu, (6) Tồn cuối kỳ.
- **FR-PM-006**: Each attribute group MUST support expand and collapse interaction triggered by clicking the group header.
- **FR-PM-007**: Expand/collapse state MUST be local to the current session and MUST NOT be persisted to the server or trigger any data change.
- **FR-PM-008**: Multiple attribute groups MAY be expanded simultaneously.
- **FR-PM-009**: Each attribute group section MUST have its own Edit button (visible when the group is expanded). Clicking a group's Edit button puts only that group's fields into inline edit mode. Save and Cancel buttons MUST appear within the group section. No modal or separate page is used. Other groups are unaffected and remain in view-only mode.
- **FR-PM-009a**: Only one attribute group per project record may be in edit mode at a time. If the user attempts to edit a second group while another is already in edit mode, the system MUST prompt them to save or cancel the current edit first.
- **FR-PM-010**: In edit mode, manual-entry fields MUST accept user input.
- **FR-PM-011**: In edit mode, formula-derived fields MUST be displayed as disabled inputs and MUST carry a "Tự tính" label (tag or badge adjacent to the field) to indicate the value is auto-calculated. These fields MUST NOT accept user input under any circumstance.
- **FR-PM-012**: The backend MUST reject any request that attempts to modify a formula-derived field, returning an appropriate error response.
- **FR-PM-013**: When displaying a formula field, if a value is stored in the database, the stored value MUST be shown.
- **FR-PM-014**: When displaying a formula field with no stored value, the backend MUST compute and return the calculated value for display.
- **FR-PM-015**: When the user clicks Save on a row in edit mode, if one or more of the changed manual-entry fields had a prior stored value, the screen MUST display a single confirmation warning dialog before committing any changes.
- **FR-PM-015a**: The warning dialog MUST be shown at most once per Save action, regardless of how many fields with prior values were changed.
- **FR-PM-016**: The warning message MUST read: "Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác".
- **FR-PM-017**: If the user confirms the warning, the save operation MUST proceed and all changed values in the group MUST be committed to the database.
- **FR-PM-018**: If the user cancels the overwrite warning dialog, no changes MUST be saved and all fields in the group MUST revert to their pre-edit values.
- **FR-PM-018a**: If the user clicks the group-level Cancel button (not the warning dialog), all fields in that group MUST revert to the values they had before the user entered edit mode for that group. No data is saved.
- **FR-PM-019**: If a manual-entry field is currently empty, the user MUST be able to enter a value without any confirmation warning.
- **FR-PM-020**: On successful save, the screen MUST display a success toast notification.
- **FR-PM-020a**: On a network or server error during save, the screen MUST display an error toast notification and MUST keep the row in edit mode with the user’s unsaved inputs intact, allowing the user to retry.
- **FR-PM-021**: On validation failure, the screen MUST display field-level error messages next to each invalid field.
- **FR-PM-022**: The screen MUST use a red primary color theme consistent with the rest of the application.
- **FR-PM-023**: The screen MUST use icons appropriate to each action (edit, save, expand/collapse, warning).
- **FR-PM-024**: The visual design MUST make it easy for users to distinguish manual-entry fields from formula-derived fields at a glance. Formula-derived fields MUST display a "Tự tính" label in both view and edit mode. Manual-entry fields MUST NOT carry this label.
- **FR-PM-025**: Draft/auto-save functionality is out of scope; the user saves changes explicitly by clicking Save within the group.

### Key Entities

- **Monthly Project Record**: Represents a project's data snapshot for a specific month. Contains ~40 fields organized into 6 logical groups. Each record belongs to exactly one project and one calendar month.
- **Attribute Group**: A logical grouping of related metrics within a Monthly Project Record (Tồn đầu kỳ, Kế hoạch tháng, Thực hiện SLSX, Kế hoạch doanh thu, Thực hiện nghiệm thu, Tồn cuối kỳ). Groups are a UI construct only and do not require separate storage.
- **Manual-Entry Field**: A field within a Monthly Project Record that the user directly inputs. Its value is not derived from other fields.
- **Formula Field**: A field within a Monthly Project Record whose value is calculated from other fields according to business rules. It can only be changed indirectly by changing its dependency fields.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate to the "Quản Lý Các Dự Án" screen and view all project records for the current month in under 3 seconds from page load.
- **SC-002**: Users can expand any attribute group within 1 click and see all field values without page navigation.
- **SC-003**: Users can complete editing and saving a manual-entry field in under 30 seconds per field.
- **SC-004**: 100% of formula-derived fields are non-editable — users cannot submit changes to formula fields under any circumstance.
- **SC-005**: 100% of existing-value overwrites trigger the confirmation warning before data is committed.
- **SC-006**: A success toast notification appears within 2 seconds of a successful save operation.
- **SC-007**: Field-level validation errors are visible without scrolling for the field in focus.
- **SC-008**: New users can distinguish editable from read-only fields without any training or documentation.

---

## Assumptions

- The underlying monthly record data structure (6 groups, 40+ fields, formula vs. manual classification) is already defined and available from Feature 003 (project-monthly-records).
- The backend API for reading and updating monthly project records already exists from Feature 003 and will be reused or extended as needed.
- The "overwrite warning" applies per Save action at the group level: if any changed field within the group had a prior stored value, the warning fires once before the group's changes are committed.
- Concurrent editing by multiple users is out of scope for this feature; last-write wins is acceptable.
- The month picker allows selection of any month, not just months within a project's active date range; the list simply shows empty results if no records exist.
- Mobile/responsive layout is a lower priority; the primary target is desktop/tablet in landscape.
- The red color theme follows the established design system already in place for the application.
- Export or print functionality is out of scope for this feature.
- Authorization and role-based access control are out of scope for this feature; any user who can reach the screen may view and edit all records.
- Draft save / auto-save is out of scope; users commit changes explicitly per group via the Save button.
