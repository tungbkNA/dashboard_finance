package com.internal.projectmgmt.dto.projecttype;

import java.util.UUID;

public record ProjectTypeResponse(
        UUID id,
        String key,
        String value) {
}
