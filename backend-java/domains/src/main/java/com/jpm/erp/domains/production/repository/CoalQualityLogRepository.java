package com.jpm.erp.domains.production.repository;

import com.jpm.erp.domains.production.entity.CoalQualityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoalQualityLogRepository extends JpaRepository<CoalQualityLog, UUID> {
}
