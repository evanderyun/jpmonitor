package com.jpmonitor.domains.logistics.entity;

import com.jpmonitor.domains.inventory.entity.SparePart;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "shipment_items")
public class ShipmentItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private GoodsShipment shipment;

    @Column(name = "shipment_id", insertable = false, updatable = false)
    private UUID shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private SparePart part;

    @Column(name = "part_id", insertable = false, updatable = false)
    private UUID partId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "unit_code_snapshot")
    private String unitCodeSnapshot;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
