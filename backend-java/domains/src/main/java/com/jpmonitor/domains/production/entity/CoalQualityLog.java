package com.jpmonitor.domains.production.entity;

import com.jpmonitor.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "coal_quality_logs")
public class CoalQualityLog extends BaseEntity {

    @Column(name = "sample_date", nullable = false)
    private LocalDate sampleDate;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "source_type", nullable = false)
    private String sourceType; // PIT, STOCKPILE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pit_id")
    private Pit pit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockpile_id")
    private Stockpile stockpile;

    @Column(name = "tm_ar")
    private BigDecimal tmAr;

    @Column(name = "im_adb")
    private BigDecimal imAdb;

    @Column(name = "ash_adb")
    private BigDecimal ashAdb;

    @Column(name = "vm_adb")
    private BigDecimal vmAdb;

    @Column(name = "fc_adb")
    private BigDecimal fcAdb;

    @Column(name = "ts_adb")
    private BigDecimal tsAdb;

    @Column(name = "gcv_adb")
    private BigDecimal gcvAdb;

    @Column(name = "gcv_ar")
    private BigDecimal gcvAr;

    @Column(name = "surveyor_name")
    private String surveyorName;
}
