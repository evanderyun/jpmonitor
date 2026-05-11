package com.jpmonitor.domains.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectDTO(
    UUID id,
    String code,
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    BigDecimal budgetLimit
) {}
