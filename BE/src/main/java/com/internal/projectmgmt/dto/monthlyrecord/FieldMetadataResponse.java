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
public class FieldMetadataResponse {

    private List<GroupMetadata> groups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMetadata {
        private String groupId;
        private String groupName;
        private List<String> manualFields;
        private List<String> formulaFields;
        private List<String> cascadedFromPrevMonthFields;
    }
}
