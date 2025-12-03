package com.jpm.erp.domains.logistics.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Java 21 Record: Immutable DTO for Shipment Items
 */
public record ShipmentItemDTO(
        UUID id,
        UUID partId,
        String partCode,
        String partName,
        Integer quantity,
        BigDecimal unitPrice,
        String notes) {
}
