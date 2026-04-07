package com.jpm.erp.api.controller;

import com.jpm.erp.domains.inventory.dto.InventoryTransactionDTO;
import com.jpm.erp.domains.inventory.dto.SparePartDTO;
import com.jpm.erp.domains.inventory.entity.InventoryTransaction;
import com.jpm.erp.domains.inventory.entity.SparePart;
import com.jpm.erp.domains.inventory.repository.InventoryTransactionRepository;
import com.jpm.erp.domains.inventory.repository.SparePartRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final SparePartRepository sparePartRepository;
    private final InventoryTransactionRepository transactionRepository;

    @GetMapping("/parts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<SparePartDTO> getAllParts(@RequestParam(required = false) String category, @RequestParam(required = false) Boolean lowStock) {
        return sparePartRepository.findAll().stream()
                .filter(p -> category == null || category.equals(p.getCategory()))
                .filter(p -> lowStock == null || !lowStock || p.getCurrentStock() <= p.getMinStockLevel())
                .map(this::mapPartToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/parts/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SparePartDTO> getPart(@PathVariable UUID id) {
        SparePart part = sparePartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Spare Part", id));
        return ResponseEntity.ok(mapPartToDTO(part));
    }

    @PostMapping("/parts")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SparePartDTO> createPart(@Valid @RequestBody SparePartDTO dto) {
        SparePart part = new SparePart();
        updatePartFromDTO(part, dto);
        return ResponseEntity.ok(mapPartToDTO(sparePartRepository.save(part)));
    }

    @PutMapping("/parts/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SparePartDTO> updatePart(@PathVariable UUID id, @Valid @RequestBody SparePartDTO dto) {
        SparePart part = sparePartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Spare Part", id));
        updatePartFromDTO(part, dto);
        return ResponseEntity.ok(mapPartToDTO(sparePartRepository.save(part)));
    }

    @DeleteMapping("/parts/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePart(@PathVariable UUID id) {
        if (!sparePartRepository.existsById(id)) throw new ResourceNotFoundException("Spare Part", id);
        sparePartRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public List<InventoryTransactionDTO> getTransactions(@RequestParam(required = false) String type, @RequestParam(required = false) UUID partId, @RequestParam(required = false) UUID supplierId, @RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate) {
        return transactionRepository.findAll().stream()
                .filter(tx -> type == null || type.equals(tx.getType()))
                .filter(tx -> partId == null || partId.equals(tx.getPart().getId()))
                .filter(tx -> supplierId == null || (tx.getSupplier() != null && supplierId.equals(tx.getSupplier().getId())))
                .filter(tx -> fromDate == null || !tx.getDate().isBefore(LocalDate.parse(fromDate)))
                .filter(tx -> toDate == null || !tx.getDate().isAfter(LocalDate.parse(toDate)))
                .map(this::mapTxToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<InventoryTransactionDTO> createTransaction(@Valid @RequestBody InventoryTransactionDTO dto) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTrxNumber("TRX-" + System.currentTimeMillis());
        tx.setDate(LocalDate.parse(dto.date()));
        tx.setType(dto.type());
        tx.setQuantity(dto.quantity());
        tx.setAppliedPrice(dto.pricePerUnit());
        tx.setReferenceId(dto.referenceId());
        tx.setNotes(dto.notes());
        tx.setPaymentMethod(dto.paymentMethod());
        tx.setDueDate(dto.dueDate() != null ? LocalDate.parse(dto.dueDate()) : null);
        tx.setPaidDate(dto.paidDate() != null ? LocalDate.parse(dto.paidDate()) : null);
        SparePart part = sparePartRepository.findById(dto.partId()).orElseThrow(() -> new ResourceNotFoundException("Spare Part", dto.partId()));
        tx.setPart(part);
        if ("USAGE".equals(dto.type())) {
            int newStock = part.getCurrentStock() - dto.quantity();
            if (newStock < 0) throw new IllegalArgumentException("Insufficient stock. Current: " + part.getCurrentStock() + ", Requested: " + dto.quantity());
            part.setCurrentStock(newStock);
        } else {
            part.setCurrentStock(part.getCurrentStock() + dto.quantity());
        }
        sparePartRepository.save(part);
        return ResponseEntity.ok(mapTxToDTO(transactionRepository.save(tx)));
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<InventoryTransactionDTO> updateTransaction(@PathVariable UUID id, @Valid @RequestBody InventoryTransactionDTO dto) {
        InventoryTransaction tx = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory Transaction", id));
        if (dto.notes() != null) tx.setNotes(dto.notes());
        if (dto.referenceId() != null) tx.setReferenceId(dto.referenceId());
        return ResponseEntity.ok(mapTxToDTO(transactionRepository.save(tx)));
    }

    private void updatePartFromDTO(SparePart part, SparePartDTO dto) {
        part.setPartNumber(dto.partNumber());
        part.setName(dto.name());
        part.setBrand(dto.brand());
        part.setCategory(dto.category());
        part.setCurrentStock(dto.currentStock());
        part.setMinStockLevel(dto.minStockLevel());
        part.setUnit(dto.unit());
        part.setRackCode(dto.location());
    }

    private SparePartDTO mapPartToDTO(SparePart p) {
        return new SparePartDTO(p.getId(), p.getPartNumber(), p.getName(), p.getBrand(), p.getCategory(), p.getCurrentStock(), p.getMinStockLevel(), p.getUnit(), p.getLocation() != null ? p.getLocation().getId() : null, p.getRackCode(), p.getCurrentPriceCash(), p.getPreferredSupplier() != null ? p.getPreferredSupplier().getId() : null);
    }

    private InventoryTransactionDTO mapTxToDTO(InventoryTransaction tx) {
        String paymentStatus = tx.getDueDate() != null ? (tx.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Pending") : null;
        return new InventoryTransactionDTO(tx.getId(), tx.getDate().toString(), tx.getType(), tx.getPart().getId(), tx.getQuantity(), tx.getAppliedPrice(), tx.getReferenceId(), tx.getEquipment() != null ? tx.getEquipment().getId() : null, tx.getSupplier() != null ? tx.getSupplier().getId() : null, tx.getNotes(), tx.getPaymentMethod(), paymentStatus, tx.getDueDate() != null ? tx.getDueDate().toString() : null, null);
    }
}
