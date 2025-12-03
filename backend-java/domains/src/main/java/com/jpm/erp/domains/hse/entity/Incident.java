package com.jpm.erp.domains.hse.entity;

import com.jpm.erp.domains.core.entity.Location;
import com.jpm.erp.domains.core.entity.Project;
import com.jpm.erp.domains.hr.entity.Employee;
import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "hse_incidents")
public class Incident extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime time;

    @Column(nullable = false)
    private String type;

    private String severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "location_detail")
    private String locationDetail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "immediate_action", columnDefinition = "TEXT")
    private String immediateAction;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;
}