package com.jpmonitor.api.controller;

import com.jpmonitor.domains.inventory.dto.InventoryTransactionDTO;
import com.jpmonitor.domains.inventory.dto.SparePartDTO;
import com.jpmonitor.domains.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // --- PARTS ---

    @GetMapping("/parts")
    public List<SparePartDTO> getAllParts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock) {
        return inventoryService.getAllParts(category, lowStock);
    }

    @GetMapping("/parts/{id}")
    public ResponseEntity<SparePartDTO> getPart(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getPart(id));
    }

    @PostMapping("/parts")
    public ResponseEntity<SparePartDTO> createPart(@Valid @RequestBody SparePartDTO dto) {
        return ResponseEntity.ok(inventoryService.createPart(dto));
    }

    @PutMapping("/parts/{id}")
    public ResponseEntity<SparePartDTO> updatePart(@PathVariable UUID id, @Valid @RequestBody SparePartDTO dto) {
        return ResponseEntity.ok(inventoryService.updatePart(id, dto));
    }

    @DeleteMapping("/parts/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable UUID id) {
        inventoryService.deletePart(id);
        return ResponseEntity.ok().build();
    }

    // --- ANALYTICS ---

    @GetMapping("/analytics")
    public ResponseEntity<Object> getAnalytics() {
        // Mock analytics data - replace with actual service call later
        List<Object> monthlyData = List.of(
                java.util.Map.of("month", "Jan", "purchase", 45, "usage", 52),
                java.util.Map.of("month", "Feb", "purchase", 62, "usage", 48),
                java.util.Map.of("month", "Mar", "purchase", 55, "usage", 61),
                java.util.Map.of("month", "Apr", "purchase", 71, "usage", 59),
                java.util.Map.of("month", "May", "purchase", 58, "usage", 67),
                java.util.Map.of("month", "Jun", "purchase", 65, "usage", 54));

        List<Object> categoryDistribution = List.of(
                java.util.Map.of("name", "Engine", "value", 450000000),
                java.util.Map.of("name", "Hydraulic", "value", 320000000),
                java.util.Map.of("name", "Undercarriage", "value", 280000000),
                java.util.Map.of("name", "Consumable", "value", 150000000),
                java.util.Map.of("name", "Electrical", "value", 200000000));

        return ResponseEntity.ok(java.util.Map.of(
                "monthlyData", monthlyData,
                "categoryDistribution", categoryDistribution));
    }

    // --- TRANSACTIONS ---

    @GetMapping("/transactions")
    public List<InventoryTransactionDTO> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID partId,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return inventoryService.getTransactions(type, partId, supplierId, fromDate, toDate);
    }

    @PostMapping("/transactions")
    public ResponseEntity<InventoryTransactionDTO> createTransaction(@Valid @RequestBody InventoryTransactionDTO dto) {
        return ResponseEntity.ok(inventoryService.createTransaction(dto));
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<InventoryTransactionDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryTransactionDTO dto) {
        return ResponseEntity.ok(inventoryService.updateTransaction(id, dto));
    }
}
