package com.jpmonitor.domains.production.entity;

import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pits")
public class Pit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String block;

    @Column(name = "strip_ratio_plan")
    private BigDecimal stripRatioPlan;

    private String status;
}