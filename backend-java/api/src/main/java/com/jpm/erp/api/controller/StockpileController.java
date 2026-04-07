package com.jpm.erp.api.controller;

import com.jpm.erp.domains.production.dto.StockpileDTO;
import com.jpm.erp.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/stockpiles")
@RequiredArgsConstructor
public class StockpileController {

    private final ProductionService productionService;

    @GetMapping
    public List<StockpileDTO> getAllStockpiles() {
        return productionService.getAllStockpiles();
    }

    @PostMapping
    public ResponseEntity<StockpileDTO> createStockpile(@RequestBody StockpileDTO dto) {
        return ResponseEntity.ok(productionService.createStockpile(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockpileDTO> updateStockpile(@PathVariable UUID id, @RequestBody StockpileDTO dto) {
        return ResponseEntity.ok(productionService.updateStockpile(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockpile(@PathVariable UUID id) {
        productionService.deleteStockpile(id);
        return ResponseEntity.ok().build();
    }
}
