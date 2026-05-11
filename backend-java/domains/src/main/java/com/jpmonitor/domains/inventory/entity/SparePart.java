package com.jpmonitor.domains.inventory.entity;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.procurement.entity.Supplier;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "spare_parts")
public class SparePart extends BaseEntity {

    @Column(name = "part_number", nullable = false, unique = true)
    private String partNumber;

    @Column(nullable = false)
    private String name;

    private String brand;
    private String category;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    @Column(nullable = false)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "location_id", insertable = false, updatable = false)
    private UUID locationId;

    @Column(name = "rack_code")
    private String rackCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_supplier_id")
    private Supplier preferredSupplier;

    @Column(name = "preferred_supplier_id", insertable = false, updatable = false)
    private UUID preferredSupplierId;

    @Column(name = "current_price_cash")
    private BigDecimal currentPriceCash;

    @Column(name = "current_price_credit")
    private BigDecimal currentPriceCredit;
}
