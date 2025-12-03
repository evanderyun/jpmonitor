package com.jpm.erp.domains.fleet.repository;

import com.jpm.erp.domains.fleet.entity.MaintenanceExternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceExternalServiceRepository extends JpaRepository<MaintenanceExternalService, UUID> {
}
