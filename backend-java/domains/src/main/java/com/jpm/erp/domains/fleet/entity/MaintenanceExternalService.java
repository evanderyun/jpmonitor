package com.jpm.erp.domains.fleet.entity;

import com.jpm.erp.domains.procurement.entity.ExternalService;
import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "maintenance_external_services")
public class MaintenanceExternalService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_record_id", nullable = false)
    private MaintenanceRecord maintenanceRecord;

    @Column(name = "maintenance_record_id", insertable = false, updatable = false)
    private UUID maintenanceRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_service_id", nullable = false)
    private ExternalService externalService;

    @Column(name = "external_service_id", insertable = false, updatable = false)
    private UUID externalServiceId;

    private BigDecimal quantity;

    @Column(name = "applied_price", nullable = false)
    private BigDecimal appliedPrice;

    @Column(name = "total_line_cost", insertable = false, updatable = false)
    private BigDecimal totalLineCost;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
