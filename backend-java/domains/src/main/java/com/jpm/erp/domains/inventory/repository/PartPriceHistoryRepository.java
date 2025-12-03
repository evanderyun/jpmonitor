package com.jpm.erp.domains.inventory.repository;

import com.jpm.erp.domains.inventory.entity.PartPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PartPriceHistoryRepository extends JpaRepository<PartPriceHistory, UUID> {
}
