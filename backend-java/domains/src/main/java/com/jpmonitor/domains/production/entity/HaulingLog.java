package com.jpmonitor.domains.production.entity;

import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.hr.entity.Employee;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "hauling_logs")
public class HaulingLog extends BaseEntity {

    @Column(name = "trx_number", nullable = false, unique = true)
    private String trxNumber;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Employee operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_pit_id")
    private Pit originPit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_stockpile_id")
    private Stockpile destinationStockpile;

    @Column(name = "distance_km")
    private BigDecimal distanceKm;

    @Column(name = "gross_weight")
    private BigDecimal grossWeight;

    @Column(name = "tare_weight")
    private BigDecimal tareWeight;

    @Column(name = "net_weight", insertable = false, updatable = false)
    private BigDecimal netWeight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loader_equipment_id")
    private Equipment loaderEquipment;

    private String status;
}
