package com.internal.projectmgmt.dto.project;

import java.util.List;

public record ProjectImportResult(
        int totalRows,
        int successCount,
        int errorCount,
        List<RowError> errors) {
    public record RowError(int row, String field, String message) {
    }
}
