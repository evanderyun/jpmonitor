package com.jpmonitor.domains.production.repository;

import com.jpmonitor.domains.production.entity.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, UUID> {

    @Query("SELECT COALESCE(SUM(p.coalMt), 0) FROM ProductionRecord p")
    BigDecimal sumTotalCoalMt();

    @Query("SELECT COALESCE(SUM(p.overburdenBcm), 0) FROM ProductionRecord p")
    BigDecimal sumTotalOverburdenBcm();

    @Query("SELECT p.date, SUM(p.coalMt), SUM(p.overburdenBcm) " +
           "FROM ProductionRecord p " +
           "WHERE p.date >= :startDate " +
           "GROUP BY p.date " +
           "ORDER BY p.date ASC")
    List<Object[]> getProductionChartData(@Param("startDate") LocalDate startDate);
    @Query("SELECT p FROM ProductionRecord p LEFT JOIN FETCH p.pit LEFT JOIN FETCH p.supervisor LEFT JOIN FETCH p.createdBy")
    List<ProductionRecord> findAllWithDetails();
}
