package com.jpm.erp.api.controller;

import com.jpm.erp.domains.analytics.dto.EnrichedDashboardStatsDTO;
import com.jpm.erp.domains.analytics.dto.EnrichedDashboardStatsDTO.*;
import com.jpm.erp.domains.analytics.dto.FleetDashboardDTO;
import com.jpm.erp.domains.analytics.dto.FleetDashboardDTO.*;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.inventory.repository.SparePartRepository;
import com.jpm.erp.domains.production.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EquipmentRepository equipmentRepository;
    private final SparePartRepository sparePartRepository;
    private final ProductionRecordRepository productionRecordRepository;

    @GetMapping("/stats")
    public ResponseEntity<EnrichedDashboardStatsDTO> getStats() {
        // 1. Production Stats (Real Data)
        BigDecimal totalCoal = productionRecordRepository.sumTotalCoalMt();
        BigDecimal totalOB = productionRecordRepository.sumTotalOverburdenBcm();
        
        BigDecimal avgSR = BigDecimal.ZERO;
        if (totalCoal.compareTo(BigDecimal.ZERO) > 0) {
            avgSR = totalOB.divide(totalCoal, 2, RoundingMode.HALF_UP);
        }

        // Get Chart Data (Last 30 Days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Object[]> chartRawData = productionRecordRepository.getProductionChartData(thirtyDaysAgo);
        
        List<ChartDataPoint> chartData = chartRawData.stream()
            .map(row -> new ChartDataPoint(
                ((LocalDate) row[0]).format(DateTimeFormatter.ofPattern("dd MMM")), // Label: "01 Dec"
                (BigDecimal) row[1], // Coal
                (BigDecimal) row[2]  // OB
            ))
            .collect(Collectors.toList());

        ProductionStats production = new ProductionStats(totalCoal, totalOB, avgSR, chartData);

        // 2. Fleet Stats (Real counts)
        long totalUnits = equipmentRepository.count();
        long operationalUnits = equipmentRepository.findAll().stream()
                .filter(e -> "Operational".equalsIgnoreCase(e.getStatus()))
                .count();
        
        BigDecimal availability = totalUnits > 0 
            ? BigDecimal.valueOf(operationalUnits).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalUnits), 1, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        FleetStats fleet = new FleetStats(
                (int) totalUnits,
                (int) operationalUnits,
                availability
        );

        // 3. Inventory Stats (Real count)
        long lowStockCount = sparePartRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() < p.getMinStockLevel())
                .count();

        InventoryStats inventory = new InventoryStats(
                (int) lowStockCount,
                new ArrayList<>() // Detailed items can be fetched via Inventory API if needed
        );

        return ResponseEntity.ok(new EnrichedDashboardStatsDTO(production, fleet, inventory));
    }

    @GetMapping("/fleet")
    public ResponseEntity<FleetDashboardDTO> getFleetStats() {
        List<Equipment> allEquipment = equipmentRepository.findAll();
        
        int totalUnits = allEquipment.size();
        int breakdownUnits = (int) allEquipment.stream()
                .filter(e -> "Breakdown".equalsIgnoreCase(e.getStatus()) || "Maintenance".equalsIgnoreCase(e.getStatus()))
                .count();
        
        double pa = totalUnits > 0 
            ? (double) (totalUnits - breakdownUnits) / totalUnits * 100.0 
            : 0.0;
        pa = Math.round(pa * 10.0) / 10.0; // Round to 1 decimal

        // Status Distribution
        Map<String, Long> statusMap = allEquipment.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus() != null ? e.getStatus() : "Unknown", Collectors.counting()));
        
        List<FleetDashboardDTO.StatusDistribution> dist = statusMap.entrySet().stream()
                .map(e -> new FleetDashboardDTO.StatusDistribution(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());

        // Predictive Maintenance
        List<FleetDashboardDTO.PredictiveMaintenance> predictive = allEquipment.stream()
                .filter(e -> e.getHourMeter() != null)
                .map(e -> {
                    int current = e.getHourMeter().intValue();
                    int next = (int) (Math.ceil((current + 1) / 250.0) * 250);
                    int remaining = next - current;
                    
                    String urgency = "Normal";
                    String color = "bg-green-500";
                    
                    if (remaining < 0) { urgency = "Overdue"; color = "bg-red-500"; }
                    else if (remaining < 50) { urgency = "Critical"; color = "bg-red-500"; }
                    else if (remaining < 100) { urgency = "Warning"; color = "bg-amber-500"; }

                    String serviceType = "PM250";
                    if (next % 2000 == 0) serviceType = "PM2000";
                    else if (next % 1000 == 0) serviceType = "PM1000";
                    else if (next % 500 == 0) serviceType = "PM500";

                    return new FleetDashboardDTO.PredictiveMaintenance(
                        e.getId(), e.getCode(), e.getModel(), current, next, serviceType, remaining, urgency, color
                    );
                })
                .sorted((a, b) -> Integer.compare(a.hoursRemaining(), b.hoursRemaining()))
                .limit(10) // Top 10 urgent
                .collect(Collectors.toList());

        FleetDashboardDTO.Analytics analytics = new FleetDashboardDTO.Analytics(totalUnits, pa, breakdownUnits, dist);
        return ResponseEntity.ok(new FleetDashboardDTO(analytics, predictive));
    }
}
