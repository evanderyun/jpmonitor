package com.jpmonitor.domains.finance.entity;

import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_allocations")
public class PaymentAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ap_id", nullable = false)
    private AccountsPayable accountsPayable;

    @Column(name = "allocated_amount", nullable = false)
    private BigDecimal allocatedAmount;
}
