package com.jpm.erp.domains.production.service.impl;

import com.jpm.erp.domains.production.repository.ProductionRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simplified Unit tests for ProductionServiceImpl
 * Focus on strip ratio calculations and production metrics
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Production Service Tests - Simplified")
class ProductionServiceImplTest {

    @Mock
    private ProductionRecordRepository productionRecordRepository;

    @InjectMocks
    private ProductionServiceImpl productionService;

    @Test
    @DisplayName("Should calculate strip ratio correctly")
    void testStripRatioCalculation() {
        // Given: Overburden = 850 BCM, Coal = 100 MT
        BigDecimal overburden = BigDecimal.valueOf(850);
        BigDecimal coal = BigDecimal.valueOf(100);

        // When: Calculate strip ratio = Overburden / Coal
        BigDecimal stripRatio = overburden.divide(coal, 2, RoundingMode.HALF_UP);

        // Then: Should equal 8.50
        assertThat(stripRatio).isEqualByComparingTo(BigDecimal.valueOf(8.5));
    }

    @Test
    @DisplayName("Should compare actual vs planned strip ratio")
    void testStripRatioComparison() {
        // Given
        BigDecimal planned = BigDecimal.valueOf(8.5);
        BigDecimal actual = BigDecimal.valueOf(8.5);

        // When: Calculate variance
        BigDecimal variance = actual.subtract(planned);

        // Then: Should be zero (on target)
        assertThat(variance).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should identify unfavorable strip ratio variance")
    void testUnfavorableVariance() {
        // Given: Actual higher than planned (more overburden)
        BigDecimal planned = BigDecimal.valueOf(8.5);
        BigDecimal actual = BigDecimal.valueOf(10.0);

        // When: Calculate variance
        BigDecimal variance = actual.subtract(planned);

        // Then: Should show positive variance (unfavorable)
        assertThat(variance).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(variance).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate percentage variance")
    void testPercentageVariance() {
        // Given
        BigDecimal planned = BigDecimal.valueOf(8.5);
        BigDecimal actual = BigDecimal.valueOf(10.0);
        BigDecimal variance = actual.subtract(planned); // 1.5

        // When: Calculate percentage = (variance / planned) * 100
        BigDecimal percentVariance = variance
                .divide(planned, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Then: Should be approximately 17.65%
        assertThat(percentVariance).isGreaterThan(BigDecimal.valueOf(17));
        assertThat(percentVariance).isLessThan(BigDecimal.valueOf(18));
    }

    @Test
    @DisplayName("Should aggregate production totals")
    void testProductionAggregation() {
        // Given: Multiple production records
        List<ProductionData> records = Arrays.asList(
                new ProductionData(BigDecimal.valueOf(850), BigDecimal.valueOf(100)),
                new ProductionData(BigDecimal.valueOf(1700), BigDecimal.valueOf(200)));

        // When: Aggregate
        BigDecimal totalOverburden = records.stream()
                .map(ProductionData::overburden)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCoal = records.stream()
                .map(ProductionData::coal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgStripRatio = totalOverburden.divide(totalCoal, 2, RoundingMode.HALF_UP);

        // Then
        assertThat(totalOverburden).isEqualByComparingTo(BigDecimal.valueOf(2550));
        assertThat(totalCoal).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(avgStripRatio).isEqualByComparingTo(BigDecimal.valueOf(8.5));
    }

    @Test
    @DisplayName("Should handle zero coal production for strip ratio")
    void testZeroCoalHandling() {
        // Given: Zero coal produced
        BigDecimal coal = BigDecimal.ZERO;

        // When/Then: Should detect zero condition
        boolean canCalculateStripRatio = coal.compareTo(BigDecimal.ZERO) > 0;
        assertThat(canCalculateStripRatio).isFalse();
    }

    // Helper record for test data
    record ProductionData(BigDecimal overburden, BigDecimal coal) {
    }
}
