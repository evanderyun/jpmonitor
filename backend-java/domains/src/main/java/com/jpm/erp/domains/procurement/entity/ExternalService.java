package com.jpm.erp.domains.procurement.entity;

import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "external_services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"supplier_id", "service_code"})
})
public class ExternalService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private UUID supplierId;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String uom; // JOB, HOUR

    @Column(name = "is_active")
    private Boolean isActive = true;
}
