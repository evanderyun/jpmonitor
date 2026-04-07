package com.jpm.erp.api.controller;
import com.jpm.erp.domains.analytics.dto.EnrichedDashboardStatsDTO;
import com.jpm.erp.domains.analytics.dto.FleetStatsDTO;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.inventory.repository.SparePartRepository;
import com.jpm.erp.domains.production.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final ProductionRecordRepository productionRepo;
    private final EquipmentRepository equipmentRepo;
    private final SparePartRepository sparePartRepo;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public EnrichedDashboardStatsDTO getStats() {
        // Production stats
        var records = productionRepo.findAll();
        double totalCoal = records.stream().filter(r -> "COAL".equals(r.getMaterialType())).mapToDouble(r -> r.getTonase() != null ? r.getTonase() : 0).sum();
        double totalOB = records.stream().filter(r -> "OVERBURDEN".equals(r.getMaterialType())).mapToDouble(r -> r.getTonase() != null ? r.getTonase() : 0).sum();
        var last30 = records.stream().filter(r -> r.getWorkingDate() != null && r.getWorkingDate().isAfter(LocalDate.now().minusDays(30))).collect(Collectors.groupingBy(r -> r.getWorkingDate().toString(), Collectors.summingDouble(r -> r.getTonase() != null ? r.getTonase() : 0)));

        // Fleet stats
        var allEq = equipmentRepo.findAll();
        long operational = allEq.stream().filter(e -> "Operational".equals(e.getStatus())).count();
        long total = allEq.size();
        double availability = total > 0 ? (operational * 100.0 / total) : 0;

        // Inventory
        long lowStock = sparePartRepo.findAll().stream().filter(p -> p.getCurrentStock() <= p.getMinStockLevel()).count();

        return new EnrichedDashboardStatsDTO(
            new EnrichedDashboardStatsDTO.ProductionStats(totalCoal, totalOB, 0.0, last30.entrySet().stream().map(e -> new EnrichedDashboardStatsDTO.ChartDataPoint(e.getKey(), java.math.BigDecimal.valueOf(e.getValue()), java.math.BigDecimal.ZERO)).collect(Collectors.toList())),
            new EnrichedDashboardStatsDTO.FleetStats((int)total, (int)operational, availability, List.of()),
            new EnrichedDashboardStatsDTO.InventoryStats((int)lowStock)
        );
    }

    @GetMapping("/fleet")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public FleetStatsDTO getFleetStats() {
        var allEq = equipmentRepo.findAll();
        var statusDist = allEq.stream().collect(Collectors.groupingBy(e -> e.getStatus() != null ? e.getStatus() : "Unknown", Collectors.counting()))
            .entrySet().stream().map(e -> new FleetStatsDTO.StatusDistribution(e.getKey(), e.getValue().intValue())).collect(Collectors.toList());
        return new FleetStatsDTO(statusDist, List.of());
    }
}
