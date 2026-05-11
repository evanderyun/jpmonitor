package com.jpmonitor.domains.inventory.repository;

import com.jpmonitor.domains.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    Optional<InventoryTransaction> findByTrxNumber(String trxNumber);
}
