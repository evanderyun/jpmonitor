package com.jpm.erp.api.controller;
import com.jpm.erp.domains.production.dto.ProductionRecordDTO;
import com.jpm.erp.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ "/api/production/records", "/api/records" })
@RequiredArgsConstructor
public class ProductionRecordController {
    private final ProductionService productionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<ProductionRecordDTO> getAllRecords() { return productionService.getAllRecords(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ProductionRecordDTO> createRecord(@RequestBody ProductionRecordDTO dto) { return ResponseEntity.ok(productionService.createRecord(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProductionRecordDTO> updateRecord(@PathVariable UUID id, @RequestBody ProductionRecordDTO dto) { return ResponseEntity.ok(productionService.updateRecord(id, dto)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) { productionService.deleteRecord(id); return ResponseEntity.ok().build(); }
}
