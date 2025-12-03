package com.jpm.erp.domains.production.entity;

import com.jpm.erp.domains.core.entity.Location;
import com.jpm.erp.domains.core.entity.Project;
import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stockpiles")
public class Stockpile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "capacity_mt")
    private BigDecimal capacityMt;

    @Column(name = "current_volume_mt")
    private BigDecimal currentVolumeMt;
}