package com.jpm.erp.domains.fleet.repository;

import com.jpm.erp.domains.fleet.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, UUID> {

        /**
         * Find daily logs by equipment and date range for lifecycle analysis
         */
        @Query("SELECT d FROM DailyLog d WHERE d.equipment.id = :equipmentId " +
                        "AND d.logDate BETWEEN :startDate AND :endDate ORDER BY d.logDate DESC")
        List<DailyLog> findByEquipmentAndDateRange(
                        @Param("equipmentId") UUID equipmentId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find daily logs by project for project-level lifecycle tracking
         */
        @Query("SELECT d FROM DailyLog d WHERE d.project.id = :projectId " +
                        "AND d.logDate BETWEEN :startDate AND :endDate ORDER BY d.logDate DESC")
        List<DailyLog> findByProjectAndDateRange(
                        @Param("projectId") UUID projectId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
