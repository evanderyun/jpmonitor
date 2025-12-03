package com.jpm.erp.domains.fleet.entity;

import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "work_order_id", insertable = false, updatable = false)
    private UUID workOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Costs
    @Column(name = "parts_cost")
    private BigDecimal partsCost;

    @Column(name = "mechanic_labor_cost")
    private BigDecimal mechanicLaborCost;

    @Column(name = "driver_labor_cost")
    private BigDecimal driverLaborCost;

    @Column(name = "meal_allowance_cost")
    private BigDecimal mealAllowanceCost;

    @Column(name = "transport_fuel_cost")
    private BigDecimal transportFuelCost;

    @Column(name = "external_service_cost")
    private BigDecimal externalServiceCost;

    @Column(name = "external_invoice_number")
    private String externalInvoiceNumber;

    @Column(name = "total_cost", insertable = false, updatable = false)
    private BigDecimal totalCost;

    @Column(name = "hm_at_service")
    private BigDecimal hmAtService;

    @Column(name = "technician_notes", columnDefinition = "TEXT")
    private String technicianNotes;

    @Column(name = "hm_reset_occurred")
    private Boolean hmResetOccurred = false;

    @Column(name = "final_hm_reading")
    private BigDecimal finalHmReading;
}
