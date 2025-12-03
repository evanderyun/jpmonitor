package com.jpm.erp.domains.production.service;

import com.jpm.erp.domains.production.dto.PitDTO;
import com.jpm.erp.domains.production.dto.ProductionRecordDTO;
import com.jpm.erp.domains.production.dto.StockpileDTO;

import java.util.List;
import java.util.UUID;

public interface ProductionService {
    // Records
    List<ProductionRecordDTO> getAllRecords();

    ProductionRecordDTO createRecord(ProductionRecordDTO dto);

    ProductionRecordDTO updateRecord(UUID id, ProductionRecordDTO dto);

    void deleteRecord(UUID id);

    // Pits
    List<PitDTO> getAllPits();

    PitDTO createPit(PitDTO dto);

    PitDTO updatePit(UUID id, PitDTO dto);

    void deletePit(UUID id);

    // Stockpiles
    List<StockpileDTO> getAllStockpiles();

    StockpileDTO createStockpile(StockpileDTO dto);

    StockpileDTO updateStockpile(UUID id, StockpileDTO dto);

    void deleteStockpile(UUID id);
}
