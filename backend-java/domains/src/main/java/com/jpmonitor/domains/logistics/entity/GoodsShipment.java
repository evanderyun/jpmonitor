package com.jpmonitor.domains.logistics.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.hr.entity.Employee;
import com.jpmonitor.domains.procurement.entity.Supplier;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "goods_shipments")
public class GoodsShipment extends BaseEntity {

    @Column(name = "do_number", nullable = false, unique = true)
    private String doNumber;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private Location sourceLocation;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_id")
    private Location targetLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_supplier_id")
    private Supplier targetSupplier;

    @Column(name = "transport_provider")
    private String transportProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_employee_id")
    private Employee driverEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_equipment_id")
    private Equipment vehicleEquipment;

    @Column(name = "external_driver_name")
    private String externalDriverName;

    @Column(name = "external_vehicle_desc")
    private String externalVehicleDesc;

    @Column(name = "police_number")
    private String policeNumber;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();
}