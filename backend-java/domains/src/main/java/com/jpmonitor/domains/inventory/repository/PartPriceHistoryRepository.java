package com.jpmonitor.domains.inventory.repository;

import com.jpmonitor.domains.inventory.entity.PartPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PartPriceHistoryRepository extends JpaRepository<PartPriceHistory, UUID> {
}
