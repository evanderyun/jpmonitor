package com.jpmonitor.domains.production.entity;

import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.hr.entity.Employee;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "production_records")
public class ProductionRecord extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String shift;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pit_id", nullable = false)
    private Pit pit;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @Column(name = "overburden_bcm")
    private BigDecimal overburdenBcm;

    @Column(name = "coal_mt")
    private BigDecimal coalMt;

    @Column(name = "stripping_ratio", insertable = false, updatable = false)
    private BigDecimal strippingRatio;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}