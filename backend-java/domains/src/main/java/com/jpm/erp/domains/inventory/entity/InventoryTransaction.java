package com.jpm.erp.domains.inventory.entity;

import com.jpm.erp.domains.core.entity.Project;
import com.jpm.erp.domains.core.entity.User;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.procurement.entity.Supplier;
import com.jpm.erp.platform.common.BaseImmutableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction extends BaseImmutableEntity {

    @Column(name = "trx_number", nullable = false, unique = true)
    private String trxNumber;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type; // IN, OUT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private SparePart part;

    @Column(name = "part_id", insertable = false, updatable = false)
    private UUID partId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "applied_price")
    private BigDecimal appliedPrice;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "reference_id")
    private String referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
