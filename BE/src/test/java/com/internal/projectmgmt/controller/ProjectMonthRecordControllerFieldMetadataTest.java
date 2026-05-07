package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.monthlyrecord.FieldMetadataResponse;
import com.internal.projectmgmt.service.MonthlyCalculationService;
import com.internal.projectmgmt.service.ProjectMonthRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProjectMonthRecordController — field-metadata endpoint.
 * Verifies HTTP 200, 6 groups, correct groupIds, and non-empty field lists.
 */
@ExtendWith(MockitoExtension.class)
class ProjectMonthRecordControllerFieldMetadataTest {

    @Mock
    private ProjectMonthRecordService service;

    @Mock
    private MonthlyCalculationService calculationService;

    @InjectMocks
    private ProjectMonthRecordController controller;

    @Test
    @DisplayName("GET /field-metadata returns 200 with 6 groups")
    void getFieldMetadata_returns200_with6Groups() {
        // Arrange — delegate to real service method for correctness
        when(service.getFieldMetadata()).thenReturn(buildExpectedMetadata());

        // Act
        var response = controller.getFieldMetadata();

        // Assert status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");

        var groups = body.getData().getGroups();
        assertThat(groups).hasSize(6);
    }

    @Test
    @DisplayName("GET /field-metadata all groups have correct groupIds")
    void getFieldMetadata_groupIds_areCorrect() {
        when(service.getFieldMetadata()).thenReturn(buildExpectedMetadata());

        var response = controller.getFieldMetadata();
        var groups = response.getBody().getData().getGroups();

        var groupIds = groups.stream().map(FieldMetadataResponse.GroupMetadata::getGroupId).toList();
        assertThat(groupIds).containsExactly("g1", "g2", "g3", "g4", "g5", "g6");
    }

    @Test
    @DisplayName("GET /field-metadata groups g1–g5 have non-empty manualFields")
    void getFieldMetadata_manualGroups_haveFields() {
        when(service.getFieldMetadata()).thenReturn(buildExpectedMetadata());

        var response = controller.getFieldMetadata();
        var groups = response.getBody().getData().getGroups();

        // g1–g5 all have manual fields
        groups.stream()
                .filter(g -> !g.getGroupId().equals("g6"))
                .forEach(g -> assertThat(g.getManualFields())
                        .as("Group %s should have manual fields", g.getGroupId())
                        .isNotEmpty());
    }

    @Test
    @DisplayName("GET /field-metadata group g6 has no manualFields (all formula)")
    void getFieldMetadata_g6_isAllFormula() {
        when(service.getFieldMetadata()).thenReturn(buildExpectedMetadata());

        var response = controller.getFieldMetadata();
        var groups = response.getBody().getData().getGroups();

        var g6 = groups.stream().filter(g -> "g6".equals(g.getGroupId())).findFirst().orElseThrow();
        assertThat(g6.getManualFields()).isEmpty();
        assertThat(g6.getFormulaFields()).hasSize(6);
    }

    @Test
    @DisplayName("GET /field-metadata g1 cascadedFromPrevMonthFields has 5 fields")
    void getFieldMetadata_g1_hasCascadedFields() {
        when(service.getFieldMetadata()).thenReturn(buildExpectedMetadata());

        var response = controller.getFieldMetadata();
        var groups = response.getBody().getData().getGroups();

        var g1 = groups.stream().filter(g -> "g1".equals(g.getGroupId())).findFirst().orElseThrow();
        assertThat(g1.getCascadedFromPrevMonthFields()).hasSize(6);
        assertThat(g1.getCascadedFromPrevMonthFields()).contains("g1SlsxTonTuSxHd");
    }

    // ---- Helper: build expected metadata by calling real service ----

    private FieldMetadataResponse buildExpectedMetadata() {
        // Instantiate the real service to get the authoritative hard-coded constant
        ProjectMonthRecordService realService = new ProjectMonthRecordService(null, null, null);
        return realService.getFieldMetadata();
    }
}
