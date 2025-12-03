package com.jpm.erp.domains.production.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockpileDTO(
    UUID id,
    String code,
    String name,
    UUID locationId,
    String locationName,
    UUID projectId,
    String projectName,
    BigDecimal capacityMt,
    BigDecimal currentVolumeMt
) {}