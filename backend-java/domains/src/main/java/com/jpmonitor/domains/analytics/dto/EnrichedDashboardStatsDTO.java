package com.jpmonitor.domains.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

// Enhanced DTO matching frontend expectations
public record EnrichedDashboardStatsDTO(
                ProductionStats production,
                FleetStats fleet,
                InventoryStats inventory) {
        public static record ProductionStats(
                        BigDecimal totalCoal,
                        BigDecimal totalOB,
                        BigDecimal avgSR,
                        List<ChartDataPoint> chartData) {
        }

        public static record FleetStats(
                        Integer total,
                        Integer operational,
                        BigDecimal availability) {
        }

        public static record InventoryStats(
                        Integer lowStockCount,
                        List<LowStockItem> lowStockItems) {
        }

        public static record ChartDataPoint(
                        String date,
                        BigDecimal OB,
                        BigDecimal Coal) {
        }

        public static record LowStockItem(
                        String id,
                        String name,
                        Integer current_stock,
                        Integer min_stock_level,
                        String part_number,
                        String unit) {
        }
}
