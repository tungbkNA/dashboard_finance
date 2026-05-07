package com.internal.projectmgmt.dto.customer;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerCode,
        String customerName) {
}
