package com.jpmonitor.api.controller;

import com.jpmonitor.domains.production.dto.PitDTO;
import com.jpmonitor.domains.production.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/pits")
@RequiredArgsConstructor
public class PitController {

    private final ProductionService productionService;

    @GetMapping
    public List<PitDTO> getAllPits() {
        return productionService.getAllPits();
    }

    @PostMapping
    public ResponseEntity<PitDTO> createPit(@RequestBody PitDTO dto) {
        return ResponseEntity.ok(productionService.createPit(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PitDTO> updatePit(@PathVariable UUID id, @RequestBody PitDTO dto) {
        return ResponseEntity.ok(productionService.updatePit(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePit(@PathVariable UUID id) {
        productionService.deletePit(id);
        return ResponseEntity.ok().build();
    }
}
