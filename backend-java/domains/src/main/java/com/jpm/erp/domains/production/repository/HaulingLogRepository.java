package com.jpm.erp.domains.production.repository;

import com.jpm.erp.domains.production.entity.HaulingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HaulingLogRepository extends JpaRepository<HaulingLog, UUID> {
    Optional<HaulingLog> findByTrxNumber(String trxNumber);
}