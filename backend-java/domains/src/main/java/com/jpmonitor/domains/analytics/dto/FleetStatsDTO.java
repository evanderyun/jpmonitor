package com.jpmonitor.domains.analytics.dto;

import java.util.List;

// Fleet-specific dashboard stats matching frontend FleetDashboard expectations
public record FleetStatsDTO(
        Analytics analytics,
        List<PredictiveMaintenance> predictiveMaint) {
    public static record Analytics(
            Integer totalUnits,
            Double pa, // Physical Availability percentage
            Integer breakdownUnits,
            List<StatusDistribution> statusDistribution) {
    }

    public static record StatusDistribution(
            String name,
            Integer value) {
    }

    public static record PredictiveMaintenance(
            String id,
            String code,
            String model,
            String serviceType,
            Integer nextServiceHM,
            Integer currentHM,
            Integer hoursRemaining,
            String urgency, // "Overdue", "Critical", "Warning", "Normal"
            String color // "bg-red-500", "bg-amber-500", "bg-green-500"
    ) {
    }
}
