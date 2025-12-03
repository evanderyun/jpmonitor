package com.jpm.erp.domains.fleet.entity;

import com.jpm.erp.domains.core.entity.Project;
import com.jpm.erp.domains.procurement.entity.Supplier;
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
@Table(name = "fuel_logs")
public class FuelLog extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "equipment_id", insertable = false, updatable = false)
    private UUID equipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private BigDecimal liters;

    @Column(name = "hour_meter")
    private BigDecimal hourMeter;

    @Column(name = "price_per_liter")
    private BigDecimal pricePerLiter;

    @Column(name = "total_cost", insertable = false, updatable = false)
    private BigDecimal totalCost; // Generated column

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "reference_doc")
    private String referenceDoc;

    @Column(name = "filled_by")
    private String filledBy;
}
