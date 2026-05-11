package com.jpmonitor.domains.fleet.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.hr.entity.Employee;
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
@Table(name = "daily_logs")
public class DailyLog extends BaseEntity {

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "equipment_id", insertable = false, updatable = false)
    private UUID equipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Employee operator;

    @Column(name = "operator_name")
    private String operatorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "start_hm", nullable = false)
    private BigDecimal startHm;

    @Column(name = "end_hm", nullable = false)
    private BigDecimal endHm;

    @Column(name = "total_hours", insertable = false, updatable = false)
    private BigDecimal totalHours;

    @Column(name = "activity_code")
    private String activityCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}