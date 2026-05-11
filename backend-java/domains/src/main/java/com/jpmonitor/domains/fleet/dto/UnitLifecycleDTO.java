package com.jpmonitor.domains.fleet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Java 21 Record: Immutable DTO for 360° Unit Lifecycle View
 * Provides comprehensive equipment tracking for ROI analysis
 */
public record UnitLifecycleDTO(
        // Equipment Identity
        UUID equipmentId,
        String equipmentCode,
        String equipmentName,
        String model,

        // Period
        LocalDate periodStart,
        LocalDate periodEnd,

        // Operational Metrics
        BigDecimal totalHoursWorked,
        BigDecimal totalKilometers,
        Integer totalWorkDays,

        // Revenue
        BigDecimal estimatedRevenue, // Based on hourly rate × hours worked
        BigDecimal hourlyRate,

        // Costs
        CostBreakdown costs,

        // ROI Calculation
        BigDecimal totalCost,
        BigDecimal netProfit,
        BigDecimal roiPercentage,

        // Activity Details
        List<DailyActivity> recentActivities,
        List<MaintenanceEvent> maintenanceHistory,
        List<FuelConsumption> fuelRecords) {

    /**
     * Nested Record: Cost breakdown using Java 21 features
     */
    public record CostBreakdown(
            BigDecimal maintenanceCost,
            BigDecimal fuelCost,
            BigDecimal operatorCost,
            BigDecimal transportCost,
            BigDecimal externalServiceCost) {
        public BigDecimal total() {
            return maintenanceCost
                    .add(fuelCost)
                    .add(operatorCost)
                    .add(transportCost)
                    .add(externalServiceCost);
        }
    }

    public record DailyActivity(
            LocalDate date,
            String activity,
            BigDecimal startHM,
            BigDecimal endHM,
            BigDecimal hoursWorked,
            String project,
            String operator) {
    }

    public record MaintenanceEvent(
            LocalDate date,
            String type,
            String description,
            BigDecimal cost,
            String status) {
    }

    public record FuelConsumption(
            LocalDate date,
            BigDecimal liters,
            BigDecimal cost,
            String project) {
    }
}
