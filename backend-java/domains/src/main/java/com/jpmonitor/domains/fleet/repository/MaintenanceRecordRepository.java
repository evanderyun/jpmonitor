package com.jpmonitor.domains.fleet.repository;

import com.jpmonitor.domains.fleet.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {

    /**
     * Find maintenance records by equipment and date range for cost analysis
     */
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.equipment.id = :equipmentId " +
            "AND m.serviceDate BETWEEN :startDate AND :endDate ORDER BY m.serviceDate DESC")
    List<MaintenanceRecord> findByEquipmentAndDateRange(
            @Param("equipmentId") UUID equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
