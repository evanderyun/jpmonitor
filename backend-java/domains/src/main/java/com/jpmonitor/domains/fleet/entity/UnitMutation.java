package com.jpmonitor.domains.fleet.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "unit_mutations")
public class UnitMutation extends BaseEntity {

    @Column(name = "mutation_number", nullable = false, unique = true)
    private String mutationNumber;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "equipment_code")
    private String equipmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private Location sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_id")
    private Location targetLocation;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "mutation_hm")
    private BigDecimal mutationHm;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "transport_unit")
    private String transportUnit;

    @Column(name = "transport_pol_number")
    private String transportPolNumber;

    @Column(name = "sender_company")
    private String senderCompany;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "recipient_company")
    private String recipientCompany;

    @Column(name = "recipient_name")
    private String recipientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private com.jpmonitor.domains.core.entity.Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}