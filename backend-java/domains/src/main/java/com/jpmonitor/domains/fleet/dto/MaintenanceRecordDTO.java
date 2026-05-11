package com.jpmonitor.domains.fleet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceRecordDTO(
    UUID id,
    UUID equipmentId,
    String woNumber,
    String startDate,
    String type,
    String description,
    String status,
    String serviceProvider, // INTERNAL / EXTERNAL
    // Costs
    BigDecimal partsCost,
    BigDecimal mechanicStoringCost, // Maps to transport_fuel_cost
    BigDecimal mechanicMealCost,    // Maps to meal_allowance_cost
    BigDecimal driverStoringCost,   // Maps to driver_labor_cost (simplification)
    BigDecimal externalCost,        // Maps to external_service_cost
    String externalInvoiceNumber
) {}
