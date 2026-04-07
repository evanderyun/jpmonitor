package com.jpm.erp.api.controller;
import com.jpm.erp.domains.procurement.dto.SupplierDTO;
import com.jpm.erp.domains.procurement.entity.Supplier;
import com.jpm.erp.domains.procurement.repository.SupplierRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public List<SupplierDTO> getAllSuppliers(@RequestParam(required = false) Boolean activeOnly) { return supplierRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable UUID id) { Supplier s = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id)); return ResponseEntity.ok(mapToDTO(s)); }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO dto) { Supplier s = new Supplier(); updateSupplierFromDTO(s, dto); s.setCode("SUP-" + System.currentTimeMillis()); return ResponseEntity.ok(mapToDTO(supplierRepository.save(s))); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @Valid @RequestBody SupplierDTO dto) { Supplier s = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id)); updateSupplierFromDTO(s, dto); return ResponseEntity.ok(mapToDTO(supplierRepository.save(s))); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) { if (!supplierRepository.existsById(id)) throw new ResourceNotFoundException("Supplier", id); supplierRepository.deleteById(id); return ResponseEntity.ok().build(); }

    private void updateSupplierFromDTO(Supplier s, SupplierDTO dto) { s.setName(dto.name()); s.setContactPerson(dto.contactPerson()); s.setPhone(dto.phone()); s.setAddress(dto.address()); s.setRating(dto.rating()); }
    private SupplierDTO mapToDTO(Supplier s) { return new SupplierDTO(s.getId(), s.getName(), "Both", s.getContactPerson(), s.getPhone(), s.getAddress(), s.getRating()); }
}
