package com.jpm.erp.api.controller;
import com.jpm.erp.domains.production.dto.StockpileDTO;
import com.jpm.erp.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/stockpiles")
@RequiredArgsConstructor
public class StockpileController {
    private final ProductionService productionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<StockpileDTO> getAllStockpiles() { return productionService.getAllStockpiles(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<StockpileDTO> createStockpile(@RequestBody StockpileDTO dto) { return ResponseEntity.ok(productionService.createStockpile(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StockpileDTO> updateStockpile(@PathVariable UUID id, @RequestBody StockpileDTO dto) { return ResponseEntity.ok(productionService.updateStockpile(id, dto)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteStockpile(@PathVariable UUID id) { productionService.deleteStockpile(id); return ResponseEntity.ok().build(); }
}
