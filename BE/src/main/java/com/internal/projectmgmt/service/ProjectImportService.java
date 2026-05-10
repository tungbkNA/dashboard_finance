package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.project.ProjectImportResult;
import com.internal.projectmgmt.dto.project.ProjectImportResult.RowError;
import com.internal.projectmgmt.entity.*;
import com.internal.projectmgmt.mapper.ProjectMapper;
import com.internal.projectmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProjectImportService {

    private static final String[] HEADERS = {
            "Mã dự án", "Tên dự án", "Mã khách hàng", "Mã loại dự án",
            "Đơn giá", "Trạng thái HĐ (NO_CONTRACT, HAS_CONTRACT)",
            "Trạng thái DA (OPEN, INPROGRESS, PENDING, DONE, CLOSE)",
            "Tháng bắt đầu (mm/yyyy)", "Tháng kết thúc (mm/yyyy)", "GĐ/PGĐ/PM (username)"
    };

    private static final Pattern MONTH_YEAR = Pattern.compile("^(0[1-9]|1[0-2])/[2-9]\\d{3}$");

    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final AppUserRepository appUserRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMonthRecordService projectMonthRecordService;

    /**
     * Generate an Excel template with headers and sample data.
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Dự án");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("PRJ-001");
            sample.createCell(1).setCellValue("Tên dự án mẫu");
            sample.createCell(2).setCellValue("KH001");
            sample.createCell(3).setCellValue("OUTSOURCE");
            sample.createCell(4).setCellValue(100000000);
            sample.createCell(5).setCellValue("NO_CONTRACT");
            sample.createCell(6).setCellValue("OPEN");
            sample.createCell(7).setCellValue("01/2025");
            sample.createCell(8).setCellValue("12/2025");
            sample.createCell(9).setCellValue("admin");

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Import projects from uploaded Excel file.
     */
    @Transactional
    public ProjectImportResult importExcel(MultipartFile file) throws IOException {
        List<RowError> errors = new ArrayList<>();
        int successCount = 0;

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                List<RowError> rowErrors = new ArrayList<>();
                int rowNum = i + 1; // 1-based for user display

                // Parse cells
                String projectCode = getCellString(row, 0).trim();
                String projectName = getCellString(row, 1).trim();
                String customerCode = getCellString(row, 2).trim();
                String projectTypeKey = getCellString(row, 3).trim();
                String priceStr = getCellString(row, 4).trim();
                String statusContractStr = getCellString(row, 5).trim().toUpperCase();
                String statusProjectStr = getCellString(row, 6).trim().toUpperCase();
                String monthStart = getCellString(row, 7).trim();
                String monthEnd = getCellString(row, 8).trim();
                String username = getCellString(row, 9).trim();

                // Validate required fields
                if (projectCode.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Mã dự án", "Không được để trống"));
                } else if (!projectCode.matches("^[A-Za-z0-9_-]{1,50}$")) {
                    rowErrors.add(new RowError(rowNum, "Mã dự án", "Chỉ chứa chữ cái, số, _ hoặc - (tối đa 50 ký tự)"));
                } else if (projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalse(projectCode)) {
                    rowErrors.add(new RowError(rowNum, "Mã dự án", "Mã dự án đã tồn tại: " + projectCode));
                }

                if (projectName.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Tên dự án", "Không được để trống"));
                } else if (projectName.length() > 255) {
                    rowErrors.add(new RowError(rowNum, "Tên dự án", "Tối đa 255 ký tự"));
                }

                // Validate customer
                Customer customer = null;
                if (customerCode.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Mã khách hàng", "Không được để trống"));
                } else {
                    Optional<Customer> opt = customerRepository
                            .findByCustomerCodeIgnoreCaseAndDeletedFalse(customerCode);
                    if (opt.isEmpty()) {
                        rowErrors.add(
                                new RowError(rowNum, "Mã khách hàng", "Không tồn tại hoặc đã bị xóa: " + customerCode));
                    } else {
                        customer = opt.get();
                    }
                }

                // Validate project type
                ProjectType projectType = null;
                if (projectTypeKey.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Mã loại dự án", "Không được để trống"));
                } else {
                    Optional<ProjectType> opt = projectTypeRepository
                            .findByKeyIgnoreCaseAndDeletedFalse(projectTypeKey);
                    if (opt.isEmpty()) {
                        rowErrors.add(new RowError(rowNum, "Mã loại dự án",
                                "Không tồn tại hoặc đã bị xóa: " + projectTypeKey));
                    } else {
                        projectType = opt.get();
                    }
                }

                // Validate price
                BigDecimal price = null;
                if (priceStr.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Đơn giá", "Không được để trống"));
                } else {
                    try {
                        price = new BigDecimal(priceStr);
                        if (price.compareTo(BigDecimal.ZERO) < 0) {
                            rowErrors.add(new RowError(rowNum, "Đơn giá", "Phải là số không âm"));
                        }
                    } catch (NumberFormatException e) {
                        rowErrors.add(new RowError(rowNum, "Đơn giá", "Không phải số hợp lệ: " + priceStr));
                    }
                }

                // Validate status contract
                StatusContract statusContract = null;
                if (statusContractStr.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Trạng thái HĐ", "Không được để trống"));
                } else {
                    try {
                        statusContract = StatusContract.valueOf(statusContractStr);
                    } catch (IllegalArgumentException e) {
                        rowErrors.add(new RowError(rowNum, "Trạng thái HĐ",
                                "Giá trị không hợp lệ: " + statusContractStr
                                        + ". Giá trị hợp lệ: NO_CONTRACT, HAS_CONTRACT"));
                    }
                }

                // Validate status project
                StatusProject statusProject = null;
                if (statusProjectStr.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Trạng thái DA", "Không được để trống"));
                } else {
                    try {
                        statusProject = StatusProject.valueOf(statusProjectStr);
                    } catch (IllegalArgumentException e) {
                        rowErrors.add(new RowError(rowNum, "Trạng thái DA",
                                "Giá trị không hợp lệ: " + statusProjectStr
                                        + ". Giá trị hợp lệ: OPEN, INPROGRESS, PENDING, DONE, CLOSE"));
                    }
                }

                // Validate month start
                if (monthStart.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Tháng bắt đầu", "Không được để trống"));
                } else if (!MONTH_YEAR.matcher(monthStart).matches()) {
                    rowErrors.add(new RowError(rowNum, "Tháng bắt đầu", "Sai định dạng mm/yyyy: " + monthStart));
                }

                // Validate month end
                if (monthEnd.isEmpty()) {
                    rowErrors.add(new RowError(rowNum, "Tháng kết thúc", "Không được để trống"));
                } else if (!MONTH_YEAR.matcher(monthEnd).matches()) {
                    rowErrors.add(new RowError(rowNum, "Tháng kết thúc", "Sai định dạng mm/yyyy: " + monthEnd));
                }

                // Validate month range
                if (!monthStart.isEmpty() && !monthEnd.isEmpty()
                        && MONTH_YEAR.matcher(monthStart).matches() && MONTH_YEAR.matcher(monthEnd).matches()) {
                    if (toYearMonth(monthEnd) < toYearMonth(monthStart)) {
                        rowErrors.add(new RowError(rowNum, "Tháng kết thúc", "Phải >= tháng bắt đầu"));
                    }
                }

                // Validate represent user (optional)
                AppUser representUser = null;
                if (!username.isEmpty()) {
                    Optional<AppUser> opt = appUserRepository.findByUsernameAndDeletedFalse(username);
                    if (opt.isEmpty()) {
                        rowErrors.add(new RowError(rowNum, "GĐ/PGĐ/PM",
                                "Username không tồn tại hoặc đã bị xóa: " + username));
                    } else if (!opt.get().isActive()) {
                        rowErrors.add(new RowError(rowNum, "GĐ/PGĐ/PM", "Tài khoản không còn hoạt động: " + username));
                    } else {
                        representUser = opt.get();
                    }
                }

                // If any errors, skip this row
                if (!rowErrors.isEmpty()) {
                    errors.addAll(rowErrors);
                    continue;
                }

                // Create project
                Project project = Project.builder()
                        .projectCode(projectCode)
                        .projectName(projectName)
                        .customer(customer)
                        .projectType(projectType)
                        .price(price)
                        .statusContract(statusContract)
                        .statusProject(statusProject)
                        .monthStart(monthStart)
                        .monthEnd(monthEnd)
                        .build();
                project.setRepresentUser(representUser);

                Project saved = projectRepository.save(project);
                projectMonthRecordService.generateRecordsForProject(saved);
                successCount++;
            }
        }

        int totalRows = successCount + (int) errors.stream().map(RowError::row).distinct().count();
        return new ProjectImportResult(totalRows, successCount, totalRows - successCount, errors);
    }

    // ---- helpers ----

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellString(row, i).trim();
                if (!val.isEmpty())
                    return false;
            }
        }
        return true;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield BigDecimal.valueOf(val).toPlainString();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                }
            }
            default -> "";
        };
    }

    private int toYearMonth(String monthYear) {
        String[] parts = monthYear.split("/");
        return Integer.parseInt(parts[1]) * 100 + Integer.parseInt(parts[0]);
    }
}
