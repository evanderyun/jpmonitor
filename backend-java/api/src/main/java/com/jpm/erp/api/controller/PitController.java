package com.jpm.erp.api.controller;
import com.jpm.erp.domains.production.dto.PitDTO;
import com.jpm.erp.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/pits")
@RequiredArgsConstructor
public class PitController {
    private final ProductionService productionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<PitDTO> getAllPits() { return productionService.getAllPits(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<PitDTO> createPit(@RequestBody PitDTO dto) { return ResponseEntity.ok(productionService.createPit(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PitDTO> updatePit(@PathVariable UUID id, @RequestBody PitDTO dto) { return ResponseEntity.ok(productionService.updatePit(id, dto)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePit(@PathVariable UUID id) { productionService.deletePit(id); return ResponseEntity.ok().build(); }
}
