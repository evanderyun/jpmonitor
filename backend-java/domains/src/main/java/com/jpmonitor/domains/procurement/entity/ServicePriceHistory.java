package com.jpmonitor.domains.procurement.entity;

import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_price_history")
public class ServicePriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ExternalService service;

    @Column(name = "service_id", insertable = false, updatable = false)
    private UUID serviceId;

    @Column(name = "price_cash", nullable = false)
    private BigDecimal priceCash;

    @Column(name = "price_credit", nullable = false)
    private BigDecimal priceCredit;

    private String currency = "IDR";

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
