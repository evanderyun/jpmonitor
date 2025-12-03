package com.jpm.erp.api.controller;

import com.jpm.erp.domains.inventory.dto.SparePartDTO;
import com.jpm.erp.domains.inventory.dto.InventoryTransactionDTO;
import com.jpm.erp.domains.inventory.entity.SparePart;
import com.jpm.erp.domains.inventory.entity.InventoryTransaction;
import com.jpm.erp.domains.inventory.repository.SparePartRepository;
import com.jpm.erp.domains.inventory.repository.InventoryTransactionRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory") // FIXED: Removed dual mapping conflict
@RequiredArgsConstructor
public class InventoryController {

    private final SparePartRepository sparePartRepository;
    private final InventoryTransactionRepository transactionRepository;

    // --- PARTS ---

    @GetMapping("/parts")
    public List<SparePartDTO> getAllParts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock) {

        // Apply filters if provided
        List<SparePart> parts;
        if (category != null || (lowStock != null && lowStock)) {
            parts = sparePartRepository.findAll().stream()
                    .filter(p -> category == null || category.equals(p.getCategory()))
                    .filter(p -> lowStock == null || !lowStock || p.getCurrentStock() < p.getMinStockLevel())
                    .collect(Collectors.toList());
        } else {
            parts = sparePartRepository.findAll();
        }

        return parts.stream()
                .map(this::mapPartToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/parts/{id}")
    public ResponseEntity<SparePartDTO> getPart(@PathVariable UUID id) {
        SparePart part = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spare Part", id));
        return ResponseEntity.ok(mapPartToDTO(part));
    }

    @PostMapping("/parts")
    public ResponseEntity<SparePartDTO> createPart(@Valid @RequestBody SparePartDTO dto) {
        SparePart part = new SparePart();
        updatePartFromDTO(part, dto);

        SparePart saved = sparePartRepository.save(part);
        return ResponseEntity.ok(mapPartToDTO(saved));
    }

    @PutMapping("/parts/{id}")
    public ResponseEntity<SparePartDTO> updatePart(@PathVariable UUID id, @Valid @RequestBody SparePartDTO dto) {
        SparePart part = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spare Part", id));

        updatePartFromDTO(part, dto);
        SparePart saved = sparePartRepository.save(part);
        return ResponseEntity.ok(mapPartToDTO(saved));
    }

    @DeleteMapping("/parts/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable UUID id) {
        if (!sparePartRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spare Part", id);
        }
        sparePartRepository.deleteById(id);
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

        List<InventoryTransaction> transactions = transactionRepository.findAll();

        // Apply filters
        return transactions.stream()
                .filter(tx -> type == null || type.equals(tx.getType()))
                .filter(tx -> partId == null || partId.equals(tx.getPart().getId()))
                .filter(tx -> supplierId == null
                        || (tx.getSupplier() != null && supplierId.equals(tx.getSupplier().getId())))
                .filter(tx -> fromDate == null || !tx.getDate().isBefore(LocalDate.parse(fromDate)))
                .filter(tx -> toDate == null || !tx.getDate().isAfter(LocalDate.parse(toDate)))
                .map(this::mapTxToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/transactions")
    public ResponseEntity<InventoryTransactionDTO> createTransaction(@Valid @RequestBody InventoryTransactionDTO dto) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTrxNumber("TRX-" + System.currentTimeMillis()); // Simple gen for now
        tx.setDate(LocalDate.parse(dto.date()));
        tx.setType(dto.type());
        tx.setQuantity(dto.quantity());
        tx.setAppliedPrice(dto.pricePerUnit());
        tx.setReferenceId(dto.referenceId());
        tx.setNotes(dto.notes());

        // Set payment fields
        tx.setPaymentMethod(dto.paymentMethod()); // Updated field name
        tx.setDueDate(dto.dueDate() != null ? LocalDate.parse(dto.dueDate()) : null);
        tx.setPaidDate(dto.paidDate() != null ? LocalDate.parse(dto.paidDate()) : null);

        // Fetch Part
        SparePart part = sparePartRepository.findById(dto.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Spare Part", dto.partId()));
        tx.setPart(part);

        // Logic update stock (Simplified for demo)
        if ("USAGE".equals(dto.type())) {
            int newStock = part.getCurrentStock() - dto.quantity();
            if (newStock < 0) {
                throw new IllegalArgumentException(
                        "Insufficient stock. Current: " + part.getCurrentStock() + ", Requested: " + dto.quantity());
            }
            part.setCurrentStock(newStock);
        } else {
            part.setCurrentStock(part.getCurrentStock() + dto.quantity());
        }
        sparePartRepository.save(part);

        InventoryTransaction saved = transactionRepository.save(tx);
        return ResponseEntity.ok(mapTxToDTO(saved));
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<InventoryTransactionDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryTransactionDTO dto) {

        InventoryTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Transaction", id));

        // Allow updating notes and reference
        if (dto.notes() != null) {
            tx.setNotes(dto.notes());
        }
        if (dto.referenceId() != null) {
            tx.setReferenceId(dto.referenceId());
        }

        InventoryTransaction saved = transactionRepository.save(tx);
        return ResponseEntity.ok(mapTxToDTO(saved));
    }

    // --- HELPER METHODS ---

    private void updatePartFromDTO(SparePart part, SparePartDTO dto) {
        part.setPartNumber(dto.partNumber());
        part.setName(dto.name());
        part.setBrand(dto.brand());
        part.setCategory(dto.category());
        part.setCurrentStock(dto.currentStock());
        part.setMinStockLevel(dto.minStockLevel());
        part.setUnit(dto.unit());
        part.setRackCode(dto.location()); // Frontend sends "location" as Rack Code string
        // part.setLocationId(dto.locationId()); // Needs fetching Location entity if
        // needed
    }

    private SparePartDTO mapPartToDTO(SparePart p) {
        return new SparePartDTO(
                p.getId(),
                p.getPartNumber(),
                p.getName(),
                p.getBrand(),
                p.getCategory(),
                p.getCurrentStock(),
                p.getMinStockLevel(),
                p.getUnit(),
                p.getLocation() != null ? p.getLocation().getId() : null,
                p.getRackCode(),
                p.getCurrentPriceCash(), // Using current price as proxy for average cost in FE
                p.getPreferredSupplier() != null ? p.getPreferredSupplier().getId() : null);
    }

    private InventoryTransactionDTO mapTxToDTO(InventoryTransaction tx) {
        // Derive payment status from due date
        String paymentStatus = null;
        if (tx.getDueDate() != null) {
            paymentStatus = tx.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Pending";
            // If there was a paid date field, we'd check that too
        }

        return new InventoryTransactionDTO(
                tx.getId(),
                tx.getDate().toString(),
                tx.getType(),
                tx.getPart().getId(),
                tx.getQuantity(),
                tx.getAppliedPrice(),
                tx.getReferenceId(),
                tx.getEquipment() != null ? tx.getEquipment().getId() : null,
                tx.getSupplier() != null ? tx.getSupplier().getId() : null,
                tx.getNotes(),
                tx.getPaymentMethod(), // paymentType
                paymentStatus, // paymentStatus (derived)
                tx.getDueDate() != null ? tx.getDueDate().toString() : null, // dueDate
                null); // paidDate (not in entity yet)
    }
}
