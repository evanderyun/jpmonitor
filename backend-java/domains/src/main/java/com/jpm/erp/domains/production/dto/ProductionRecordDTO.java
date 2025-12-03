package com.jpm.erp.domains.production.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductionRecordDTO(
        UUID id,
        String date,
        String shift,
        UUID pitId,
        String pitName,
        UUID supervisorId, // ✅ Added for service compatibility
        String supervisorName, // ✅ Added for frontend display
        BigDecimal overburdenBcm,
        BigDecimal coalMt,
        BigDecimal strippingRatio,
        String status,
        String notes // ✅ Added for additional info
) {
}