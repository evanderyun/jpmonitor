package com.jpmonitor.domains.fleet.repository;

import com.jpmonitor.domains.fleet.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, UUID> {

        /**
         * Find fuel logs by equipment and date range for fuel cost analysis
         */
        @Query("SELECT f FROM FuelLog f WHERE f.equipment.id = :equipmentId " +
                        "AND f.date BETWEEN :startDate AND :endDate ORDER BY f.date DESC")
        List<FuelLog> findByEquipmentAndDateRange(
                        @Param("equipmentId") UUID equipmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
