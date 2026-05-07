package com.internal.projectmgmt.exception;

import java.util.List;

/**
 * Thrown by ProjectService.update() when the new month range would deactivate
 * existing
 * monthly records and confirmShrink=false. The caller (controller) must ask the
 * user
 * to confirm before re-submitting with confirmShrink=true.
 */
public class ShrinkWarningException extends RuntimeException {

    private final List<String> pendingInactiveMonths;

    public ShrinkWarningException(List<String> pendingInactiveMonths) {
        super("Phạm vi tháng thu hẹp sẽ vô hiệu hóa " + pendingInactiveMonths.size() + " bản ghi tháng");
        this.pendingInactiveMonths = pendingInactiveMonths;
    }

    public List<String> getPendingInactiveMonths() {
        return pendingInactiveMonths;
    }
}
