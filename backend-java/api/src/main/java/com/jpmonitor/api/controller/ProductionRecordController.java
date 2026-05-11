package com.jpmonitor.api.controller;

import com.jpmonitor.domains.production.dto.ProductionRecordDTO;
import com.jpmonitor.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ "/api/production/records", "/api/records" })
@RequiredArgsConstructor
public class ProductionRecordController {

    private final ProductionService productionService;

    @GetMapping
    public List<ProductionRecordDTO> getAllRecords() {
        return productionService.getAllRecords();
    }

    @PostMapping
    public ResponseEntity<ProductionRecordDTO> createRecord(@RequestBody ProductionRecordDTO dto) {
        return ResponseEntity.ok(productionService.createRecord(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionRecordDTO> updateRecord(@PathVariable UUID id,
            @RequestBody ProductionRecordDTO dto) {
        return ResponseEntity.ok(productionService.updateRecord(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        productionService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
}
