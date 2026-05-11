package com.jpmonitor.domains.hr.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.Project;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false)
    private String name;

    private String position;
    private String department;
    private String email;
    private String phone;
    private String status; // Active

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_project_id")
    private Project currentProject;

    @Column(name = "current_project_id", insertable = false, updatable = false)
    private UUID currentProjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "location_id", insertable = false, updatable = false)
    private UUID locationId;

    @Column(name = "joined_date")
    private LocalDate joinedDate;
}
