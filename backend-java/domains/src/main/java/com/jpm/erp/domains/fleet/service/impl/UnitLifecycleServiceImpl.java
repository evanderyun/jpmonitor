package com.jpm.erp.domains.fleet.service.impl;

import com.jpm.erp.domains.fleet.dto.UnitLifecycleDTO;
import com.jpm.erp.domains.fleet.dto.UnitLifecycleDTO.*;
import com.jpm.erp.domains.fleet.entity.DailyLog;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.entity.FuelLog;
import com.jpm.erp.domains.fleet.entity.MaintenanceRecord;
import com.jpm.erp.domains.fleet.repository.DailyLogRepository;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.fleet.repository.FuelLogRepository;
import com.jpm.erp.domains.fleet.repository.MaintenanceRecordRepository;
import com.jpm.erp.domains.fleet.service.UnitLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Java 21 Implementation of Unit Lifecycle Service
 * Uses Records, Pattern Matching, and Stream API
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitLifecycleServiceImpl implements UnitLifecycleService {

        private final EquipmentRepository equipmentRepository;
        private final DailyLogRepository dailyLogRepository;
        private final MaintenanceRecordRepository maintenanceRepository;
        private final FuelLogRepository fuelLogRepository;

        private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("500000"); // Rp 500K/hour
        private static final BigDecimal OPERATOR_COST_PER_DAY = new BigDecimal("150000"); // Rp 150K/day

        @Override
        public UnitLifecycleDTO getUnitLifecycle(UUID equipmentId, LocalDate startDate, LocalDate endDate) {
                Equipment equipment = equipmentRepository.findById(equipmentId)
                                .orElseThrow(() -> new RuntimeException("Equipment not found: " + equipmentId));

                // Fetch all data in parallel using Virtual Threads (enabled globally)
                List<DailyLog> dailyLogs = dailyLogRepository.findByEquipmentAndDateRange(equipmentId, startDate,
                                endDate);
                List<MaintenanceRecord> maintenanceRecords = maintenanceRepository.findByEquipmentAndDateRange(
                                equipmentId,
                                startDate, endDate);
                List<FuelLog> fuelLogs = fuelLogRepository.findByEquipmentAndDateRange(equipmentId, startDate, endDate);

                // Calculate operational metrics
                BigDecimal totalHours = dailyLogs.stream()
                                .map(log -> log.getEndHm().subtract(log.getStartHm())) // ✅ Fixed: getEndHm() not
                                                                                       // getEndHM()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Integer workDays = (int) dailyLogs.stream()
                                .map(DailyLog::getLogDate) // ✅ Fixed: getLogDate() not getDate()
                                .distinct()
                                .count();

                // Calculate costs using nested records
                CostBreakdown costs = calculateCostBreakdown(maintenanceRecords, fuelLogs, workDays);

                BigDecimal estimatedRevenue = totalHours.multiply(DEFAULT_HOURLY_RATE);
                BigDecimal totalCost = costs.total();
                BigDecimal netProfit = estimatedRevenue.subtract(totalCost);
                BigDecimal roiPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                                ? netProfit.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                                : BigDecimal.ZERO;

                // Convert to DTOs using Java 21 patterns
                List<DailyActivity> activities = dailyLogs.stream()
                                .limit(10) // Recent 10
                                .map(this::toDailyActivity)
                                .toList(); // Java 21 toList()

                List<MaintenanceEvent> maintenance = maintenanceRecords.stream()
                                .map(this::toMaintenanceEvent)
                                .toList();

                List<FuelConsumption> fuel = fuelLogs.stream()
                                .map(this::toFuelConsumption)
                                .toList();

                return new UnitLifecycleDTO(
                                equipment.getId(),
                                equipment.getCode(),
                                equipment.getName(),
                                equipment.getModel(),
                                startDate,
                                endDate,
                                totalHours,
                                BigDecimal.ZERO, // kilometers - to be implemented
                                workDays,
                                estimatedRevenue,
                                DEFAULT_HOURLY_RATE,
                                costs,
                                totalCost,
                                netProfit,
                                roiPercentage,
                                activities,
                                maintenance,
                                fuel);
        }

        @Override
        public List<UnitLifecycleDTO> getAllUnitsLifecycle(LocalDate startDate, LocalDate endDate) {
                return equipmentRepository.findAll().stream()
                                .map(eq -> getUnitLifecycle(eq.getId(), startDate, endDate))
                                .toList();
        }

        @Override
        public List<UnitLifecycleDTO> getProjectUnitsLifecycle(UUID projectId, LocalDate startDate, LocalDate endDate) {
                // Get equipment used in project during period
                List<UUID> equipmentIds = dailyLogRepository
                                .findByProjectAndDateRange(projectId, startDate, endDate)
                                .stream()
                                .map(log -> log.getEquipment().getId())
                                .distinct()
                                .toList();

                return equipmentIds.stream()
                                .map(id -> getUnitLifecycle(id, startDate, endDate))
                                .toList();
        }

        @Override
        public CostBreakdown calculateUnitCosts(UUID equipmentId, LocalDate startDate, LocalDate endDate) {
                List<MaintenanceRecord> maintenanceRecords = maintenanceRepository
                                .findByEquipmentAndDateRange(equipmentId, startDate, endDate);
                List<FuelLog> fuelLogs = fuelLogRepository
                                .findByEquipmentAndDateRange(equipmentId, startDate, endDate);

                int workDays = (int) dailyLogRepository
                                .findByEquipmentAndDateRange(equipmentId, startDate, endDate)
                                .stream()
                                .map(DailyLog::getLogDate) // Correct getter method
                                .distinct()
                                .count();

                return calculateCostBreakdown(maintenanceRecords, fuelLogs, workDays);
        }

        // ========== Private Helper Methods ==========

        private CostBreakdown calculateCostBreakdown(
                        List<MaintenanceRecord> maintenanceRecords,
                        List<FuelLog> fuelLogs,
                        Integer workDays) {
                BigDecimal maintenanceCost = maintenanceRecords.stream()
                                .map(m -> {
                                        BigDecimal parts = m.getPartsCost() != null ? m.getPartsCost()
                                                        : BigDecimal.ZERO;
                                        BigDecimal meal = m.getMealAllowanceCost() != null ? m.getMealAllowanceCost()
                                                        : BigDecimal.ZERO;
                                        BigDecimal driver = m.getDriverLaborCost() != null ? m.getDriverLaborCost()
                                                        : BigDecimal.ZERO;
                                        BigDecimal external = m.getExternalServiceCost() != null
                                                        ? m.getExternalServiceCost()
                                                        : BigDecimal.ZERO;
                                        return parts.add(meal).add(driver).add(external);
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal transportCost = maintenanceRecords.stream()
                                .map(m -> m.getTransportFuelCost() != null ? m.getTransportFuelCost() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal fuelCost = fuelLogs.stream()
                                .map(f -> f.getTotalCost() != null ? f.getTotalCost() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal externalCost = maintenanceRecords.stream()
                                .map(m -> m.getExternalServiceCost() != null ? m.getExternalServiceCost()
                                                : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal operatorCost = OPERATOR_COST_PER_DAY.multiply(new BigDecimal(workDays));

                return new CostBreakdown(
                                maintenanceCost,
                                fuelCost,
                                operatorCost,
                                transportCost,
                                externalCost);
        }

        private DailyActivity toDailyActivity(DailyLog log) {
                BigDecimal hoursWorked = log.getEndHm().subtract(log.getStartHm());
                return new DailyActivity(
                                log.getLogDate(),
                                log.getActivityCode(),
                                log.getStartHm(),
                                log.getEndHm(),
                                hoursWorked,
                                log.getProject() != null ? log.getProject().getName() : "N/A",
                                log.getOperator() != null ? log.getOperator().getName() : "N/A");
        }

        private MaintenanceEvent toMaintenanceEvent(MaintenanceRecord record) {
                BigDecimal totalCost = BigDecimal.ZERO;
                if (record.getPartsCost() != null)
                        totalCost = totalCost.add(record.getPartsCost());
                if (record.getExternalServiceCost() != null)
                        totalCost = totalCost.add(record.getExternalServiceCost());

                return new MaintenanceEvent(
                                record.getServiceDate(),
                                record.getWorkOrder() != null ? record.getWorkOrder().getType() : "UNKNOWN",
                                record.getDescription(),
                                totalCost,
                                record.getWorkOrder() != null ? record.getWorkOrder().getStatus() : "COMPLETED");
        }

        private FuelConsumption toFuelConsumption(FuelLog log) {
                return new FuelConsumption(
                                log.getDate(), // ✅ Fixed: getDate() not getFuelDate()
                                log.getLiters(),
                                log.getTotalCost(),
                                log.getProject() != null ? log.getProject().getName() : "N/A");
        }
}
