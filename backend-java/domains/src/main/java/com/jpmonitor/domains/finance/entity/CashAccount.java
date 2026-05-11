package com.jpmonitor.domains.finance.entity;

import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cash_accounts")
public class CashAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String type; // BANK, CASH
    private String currency;

    private BigDecimal balance;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
