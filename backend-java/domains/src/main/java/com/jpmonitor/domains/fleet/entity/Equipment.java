package com.jpmonitor.domains.fleet.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String model;
    private String type;
    private String brand;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "engine_number")
    private String engineNumber;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "hour_meter")
    private BigDecimal hourMeter;

    private BigDecimal kilometer;
    private String owner;
}