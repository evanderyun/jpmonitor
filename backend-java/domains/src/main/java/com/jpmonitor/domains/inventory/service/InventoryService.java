package com.jpmonitor.domains.inventory.service;

import com.jpmonitor.domains.inventory.dto.InventoryTransactionDTO;
import com.jpmonitor.domains.inventory.dto.SparePartDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    // ==================== PARTS ====================

    List<SparePartDTO> getAllParts(String category, Boolean lowStock);

    SparePartDTO getPart(UUID id);

    SparePartDTO createPart(SparePartDTO dto);

    SparePartDTO updatePart(UUID id, SparePartDTO dto);

    void deletePart(UUID id);

    // ==================== TRANSACTIONS ====================

    List<InventoryTransactionDTO> getTransactions(String type, UUID partId, UUID supplierId,
                                                   String fromDate, String toDate);

    InventoryTransactionDTO createTransaction(InventoryTransactionDTO dto);

    InventoryTransactionDTO updateTransaction(UUID id, InventoryTransactionDTO dto);
}
