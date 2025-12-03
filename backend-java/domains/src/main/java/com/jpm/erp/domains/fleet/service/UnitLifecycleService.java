package com.jpm.erp.domains.fleet.service;

import com.jpm.erp.domains.fleet.dto.UnitLifecycleDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for comprehensive Unit Lifecycle Tracking
 * Provides 360° view of equipment for ROI calculation
 */
public interface UnitLifecycleService {

    /**
     * Get complete lifecycle view for a specific unit
     * Aggregates: Daily Logs + Maintenance + Fuel + Costs
     */
    UnitLifecycleDTO getUnitLifecycle(UUID equipmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get lifecycle summary for all units in a period
     */
    List<UnitLifecycleDTO> getAllUnitsLifecycle(LocalDate startDate, LocalDate endDate);

    /**
     * Get lifecycle filtered by project
     */
    List<UnitLifecycleDTO> getProjectUnitsLifecycle(UUID projectId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate ROI for a specific unit
     */
    UnitLifecycleDTO.CostBreakdown calculateUnitCosts(UUID equipmentId, LocalDate startDate, LocalDate endDate);
}
