package com.internal.projectmgmt.dto.monthlyrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossMonthPropagationResult {

    private List<String> affectedMonthKeys;

    public enum StoppedReason {
        NO_MORE_MONTHS,
        INACTIVE_MONTH,
        LOCKED_MONTH,
        NO_CHANGE_DETECTED
    }
}
