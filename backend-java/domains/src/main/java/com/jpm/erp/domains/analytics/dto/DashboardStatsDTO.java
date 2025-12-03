package com.jpm.erp.domains.analytics.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardStatsDTO(
        BigDecimal totalProduction,
        BigDecimal totalFuelUsed,
        BigDecimal totalCost,
        Integer activeUnits,
        Integer activeOperators,
        Map<String, BigDecimal> productionTrend,
        Map<String, BigDecimal> costTrend) {
}
