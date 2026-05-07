# Quickstart: Quản Lý Các Dự Án

**Feature**: 004-project-management-view  
**Date**: 2026-05-07

---

## Prerequisites

- Java 21 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node.js 20+ (`node -v`)
- PostgreSQL 15+ running on `localhost:5432`
- Database `dashboard_finance` exists (created by Feature 001)

---

## Backend

```powershell
cd BE

# Run all tests (excluding broken package test)
mvn test -Dtest="!DashboardFinanceApplicationTests"

# Start dev server (port 8080)
mvn spring-boot:run
```

Backend is ready when you see:
```
Started DashboardFinanceApplication in X.XXX seconds
```

Swagger UI: http://localhost:8080/swagger-ui.html  
Health check: `GET http://localhost:8080/api/binance/health`

### New endpoint for this feature

```
GET http://localhost:8080/api/binance/project-monthly-records/field-metadata
```

---

## Frontend

```powershell
cd FE

# Install dependencies (first time only)
npm install

# TypeScript type check
npx vue-tsc --noEmit

# Start dev server (port 5173)
npm run dev
```

Frontend dev URL: http://localhost:5173

Navigate to **Quản Lý Các Dự Án** in the sidebar (route `/projects`).

---

## Environment Variables

`FE/.env`:
```
VITE_API_BASE_URL=http://localhost:8080
```

---

## Testing the Feature Manually

1. Start BE and FE.
2. Go to **Cài đặt dự án** → create a project with month range including current month (e.g. 01/2026 – 12/2026).
3. Navigate to **Quản Lý Các Dự Án**.
4. Verify the project appears as a card in the accordion list for the current month.
5. Expand the project card → 6 group sections appear collapsed.
6. Expand a group → fields shown with "Tự tính" labels on formula fields.
7. Click **Sửa** on a group → fields become editable inputs; Save + Cancel appear.
8. Edit a field that already has data → clicking Save shows the overwrite warning dialog.
9. Confirm → data saved, success toast.
10. Cancel → group reverts to pre-edit values.
11. Change the month filter → list updates.
12. Select a month with no records → empty state "Không có dữ liệu dự án cho tháng này".
