package com.jpmonitor.domains.finance.entity;

import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.domains.procurement.entity.Supplier;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "accounts_payable")
public class AccountsPayable extends BaseEntity {

    @Column(name = "ap_number", nullable = false, unique = true)
    private String apNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private UUID supplierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_category_id")
    private CostCategory costCategory;

    @Column(name = "source_module", nullable = false)
    private String sourceModule;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_doc_number")
    private String referenceDocNumber;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "outstanding_amount", insertable = false, updatable = false)
    private BigDecimal outstandingAmount;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
