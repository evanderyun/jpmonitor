package com.jpm.erp.domains.procurement.repository;

import com.jpm.erp.domains.procurement.entity.ExternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, UUID> {
}
