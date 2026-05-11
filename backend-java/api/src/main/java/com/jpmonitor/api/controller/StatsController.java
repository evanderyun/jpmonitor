package com.jpmonitor.api.controller;

import com.jpmonitor.domains.analytics.dto.EnrichedDashboardStatsDTO;
import com.jpmonitor.domains.analytics.dto.EnrichedDashboardStatsDTO.*;
import com.jpmonitor.domains.analytics.dto.FleetStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class StatsController {

    private List<FleetStatsDTO.StatusDistribution> generateStatusDistribution() {
        return List.of(
                new FleetStatsDTO.StatusDistribution("Operational", 18),
                new FleetStatsDTO.StatusDistribution("Breakdown", 5),
                new FleetStatsDTO.StatusDistribution("Maintenance", 2));
    }

    private List<FleetStatsDTO.PredictiveMaintenance> generatePredictiveMaintenance() {
        return List.of(
                new FleetStatsDTO.PredictiveMaintenance(
                        "1", "EX-401", "Komatsu PC200",
                        "Oil Change", 2500, 2480, 20,
                        "Critical", "bg-red-500"),
                new FleetStatsDTO.PredictiveMaintenance(
                        "2", "DT-107", "HD785-7",
                        "PM Service", 3000, 2750, 250,
                        "Warning", "bg-amber-500"),
                new FleetStatsDTO.PredictiveMaintenance(
                        "3", "EX-305", "Hitachi ZX470",
                        "Filter Change", 2250, 1800, 450,
                        "Normal", "bg-green-500"));
    }

    private List<ChartDataPoint> generateChartData() {
        return List.of(
                new ChartDataPoint("W1", new BigDecimal("1200"), new BigDecimal("180")),
                new ChartDataPoint("W2", new BigDecimal("1400"), new BigDecimal("210")),
                new ChartDataPoint("W3", new BigDecimal("1350"), new BigDecimal("195")),
                new ChartDataPoint("W4", new BigDecimal("1500"), new BigDecimal("220")));
    }
}
