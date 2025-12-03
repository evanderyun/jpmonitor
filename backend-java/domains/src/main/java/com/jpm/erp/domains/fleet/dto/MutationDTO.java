package com.jpm.erp.domains.fleet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MutationDTO(
        UUID id,
        String type,
        UUID equipmentId,
        String equipmentCode,
        String departureDate,
        String arrivalDate,
        BigDecimal mutationHM,
        String status,
        String sourceLocation,
        String targetLocation,
        String referenceDocument) {
}
