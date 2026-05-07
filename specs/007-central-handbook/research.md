# Research: Sổ tay trung tâm

**Feature**: 007-central-handbook  
**Date**: 2026-05-07

## 1. Delete Strategy: Hard Delete vs Soft Delete

**Decision**: Hard delete for both FileGroup and FileRecord  
**Rationale**: This module stores external URL bookmarks — no audit trail needed for deleted links. Hard delete keeps queries simple (no `deleted=false` filters). FK constraint prevents orphan data naturally.  
**Alternatives considered**:
- Soft delete (like ProjectType): Rejected because FileRecord has no downstream dependencies and link bookmarks are not audit-sensitive. Adds unnecessary complexity for a simple catalog.

## 2. Search Implementation: Server-side vs Client-side

**Decision**: Server-side search with JPQL `LIKE` query + pagination  
**Rationale**: FR-009/FR-010 require search by name + filter by group. Server-side is consistent with spec assumption about server pagination for large datasets. JPQL `LOWER(f.fileName) LIKE LOWER(:keyword)` handles case-insensitive search.  
**Alternatives considered**:
- Client-side filtering (load all, filter in JS): Rejected per constitution §10.2 — filters should not send unused data. Won't scale to 100+ records per SC-002.

## 3. API Path Prefix

**Decision**: `/api/handbook/file-groups` and `/api/handbook/file-records`  
**Rationale**: Groups endpoints under a `handbook` namespace matching the module name. Follows RESTful resource naming (constitution §6.2). Existing pattern uses `/api/binance/...` but that prefix is domain-specific to the original project name — new module uses its own namespace.  
**Alternatives considered**:
- `/api/binance/file-groups`: Rejected — `binance` prefix is legacy naming, doesn't match module concept.
- `/api/file-groups` (flat): Rejected — no module grouping, harder to apply path-based security rules.

## 4. Permission Seeding

**Decision**: Add `MANAGE_HANDBOOK` permission to V11 migration + assign to admin role  
**Rationale**: Follows existing pattern from V7 (seed permissions/roles). Single migration seeds both default file groups and the permission.  
**Alternatives considered**:
- Application-level seed (CommandLineRunner): Rejected — existing pattern uses Flyway migrations for all seed data (V7__seed_permissions_and_roles.sql).

## 5. FileRecord.createdBy Storage

**Decision**: Store `created_by` as `VARCHAR(50)` containing the username (not UUID FK to app_user)  
**Rationale**: Follows denormalized pattern — simpler queries, no JOIN needed for display. If user is deleted/renamed, the original creator name is preserved. The `Authentication.getName()` returns the username from JWT.  
**Alternatives considered**:
- FK to app_user.id (UUID): Rejected — adds complexity for a display-only field. Requires JOIN on every list query. User deletion would need CASCADE or SET NULL handling.

## 6. Frontend State Management

**Decision**: No Pinia store — use local component state with API calls  
**Rationale**: Both screens are simple CRUD lists with no shared state. Data is always fresh from API. Constitution §5.1 says "Pinia (khi cần state dùng chung)" — not needed here.  
**Alternatives considered**:
- Pinia store per entity: Rejected — over-engineering for isolated CRUD screens with no cross-component data sharing.

## 7. Inactive Group Toggle in File List

**Decision**: Query parameter `includeInactive=true/false` (default false) sent to API, which filters at DB level  
**Rationale**: Server-side filtering is consistent with search approach. Keeps FE logic minimal — just pass a boolean toggle state to API.  
**Alternatives considered**:
- Client-side filter (load all files, hide inactive groups in JS): Rejected — inconsistent with server-side search pattern, breaks pagination.
