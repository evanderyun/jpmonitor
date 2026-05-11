package com.jpmonitor.api.controller;

import com.jpmonitor.domains.procurement.dto.SupplierDTO;
import com.jpmonitor.domains.procurement.entity.Supplier;
import com.jpmonitor.domains.procurement.repository.SupplierRepository;
import com.jpmonitor.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @GetMapping
    public List<SupplierDTO> getAllSuppliers(@RequestParam(required = false) Boolean activeOnly) {
        // Note: Supplier entity doesn't have status field currently
        // Active filtering would need to be implemented when status field is added to
        // entity
        return supplierRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        return ResponseEntity.ok(mapToDTO(supplier));
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        updateSupplierFromDTO(supplier, dto);
        supplier.setCode("SUP-" + System.currentTimeMillis());

        Supplier saved = supplierRepository.save(supplier);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        updateSupplierFromDTO(supplier, dto);
        Supplier saved = supplierRepository.save(supplier);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier", id);
        }
        supplierRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods
    private void updateSupplierFromDTO(Supplier supplier, SupplierDTO dto) {
        supplier.setName(dto.name());
        supplier.setContactPerson(dto.contactPerson());
        supplier.setPhone(dto.phone());
        supplier.setAddress(dto.address());
        supplier.setRating(dto.rating());
        // Type is simplified in DTO, not stored in entity
    }

    private SupplierDTO mapToDTO(Supplier s) {
        return new SupplierDTO(
                s.getId(),
                s.getName(),
                "Both", // Simplified logic for now, maybe derive from services
                s.getContactPerson(),
                s.getPhone(),
                s.getAddress(),
                s.getRating());
    }
}
