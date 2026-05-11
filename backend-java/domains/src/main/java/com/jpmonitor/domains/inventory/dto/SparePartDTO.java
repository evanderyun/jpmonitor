package com.jpmonitor.domains.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SparePartDTO(
    UUID id,
    String partNumber,
    String name,
    String brand,
    String category,
    Integer currentStock,
    Integer minStockLevel,
    String unit,
    UUID locationId,
    String location, // For display name or rack code
    BigDecimal averageCost,
    UUID preferredSupplierId
) {}
