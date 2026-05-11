package com.jpmonitor.domains.production.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PitDTO(
    UUID id,
    String code,
    String name,
    String block,
    BigDecimal stripRatioPlan
) {}