package com.jpmonitor.api.controller;

import com.jpmonitor.domains.fleet.dto.UnitMutationDTO;
import com.jpmonitor.domains.fleet.service.UnitMutationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ "/api/mutations", "/api/fleet/mutations" }) // Alias to catch both
@RequiredArgsConstructor
public class UnitMutationController {

    private final UnitMutationService mutationService;

    @GetMapping
    public List<UnitMutationDTO> getAllMutations() {
        return mutationService.getAllMutations();
    }

    @PostMapping
    public ResponseEntity<UnitMutationDTO> createMutation(@RequestBody UnitMutationDTO dto) {
        return ResponseEntity.ok(mutationService.createMutation(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitMutationDTO> updateMutation(@PathVariable UUID id, @RequestBody UnitMutationDTO dto) {
        return ResponseEntity.ok(mutationService.updateMutation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMutation(@PathVariable UUID id) {
        mutationService.deleteMutation(id);
        return ResponseEntity.ok().build();
    }
}
