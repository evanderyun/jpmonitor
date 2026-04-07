package com.jpm.erp.api.controller;
import com.jpm.erp.domains.fleet.dto.UnitMutationDTO;
import com.jpm.erp.domains.fleet.service.UnitMutationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ "/api/mutations", "/api/fleet/mutations" })
@RequiredArgsConstructor
public class UnitMutationController {
    private final UnitMutationService mutationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<UnitMutationDTO> getAllMutations() { return mutationService.getAllMutations(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<UnitMutationDTO> createMutation(@RequestBody UnitMutationDTO dto) { return ResponseEntity.ok(mutationService.createMutation(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UnitMutationDTO> updateMutation(@PathVariable UUID id, @RequestBody UnitMutationDTO dto) { return ResponseEntity.ok(mutationService.updateMutation(id, dto)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMutation(@PathVariable UUID id) { mutationService.deleteMutation(id); return ResponseEntity.ok().build(); }
}
