package com.jpmonitor.domains.analytics.dto;

import java.util.List;
import java.util.UUID;

public record FleetDashboardDTO(
    Analytics analytics,
    List<PredictiveMaintenance> predictiveMaint
) {
    public record Analytics(
        int totalUnits,
        double pa,
        int breakdownUnits,
        List<StatusDistribution> statusDistribution
    ) {}

    public record StatusDistribution(
        String name,
        int value
    ) {}

    public record PredictiveMaintenance(
        UUID id,
        String code,
        String model,
        int currentHM,
        int nextServiceHM,
        String serviceType,
        int hoursRemaining,
        String urgency,
        String color
    ) {}
}
