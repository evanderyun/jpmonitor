package com.jpmonitor.domains.logistics.service.impl;

import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.fleet.repository.EquipmentRepository;
import com.jpmonitor.domains.hr.repository.EmployeeRepository;
import com.jpmonitor.domains.inventory.entity.InventoryTransaction;
import com.jpmonitor.domains.inventory.entity.SparePart;
import com.jpmonitor.domains.inventory.repository.InventoryTransactionRepository;
import com.jpmonitor.domains.inventory.repository.SparePartRepository;
import com.jpmonitor.domains.logistics.dto.GoodsShipmentDTO;
import com.jpmonitor.domains.logistics.dto.ShipmentItemDTO;
import com.jpmonitor.domains.logistics.entity.GoodsShipment;
import com.jpmonitor.domains.logistics.entity.ShipmentItem;
import com.jpmonitor.domains.logistics.repository.GoodsShipmentRepository;
import com.jpmonitor.domains.logistics.service.GoodsShipmentService;
import com.jpmonitor.domains.procurement.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsShipmentServiceImpl implements GoodsShipmentService {

    private final GoodsShipmentRepository shipmentRepository;
    private final LocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final EquipmentRepository equipmentRepository;
    private final SparePartRepository sparePartRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GoodsShipmentDTO> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsShipmentDTO getShipment(UUID id) {
        GoodsShipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        return mapToDTO(shipment);
    }

    @Override
    @Transactional
    public GoodsShipmentDTO createShipment(GoodsShipmentDTO dto) {
        GoodsShipment shipment = new GoodsShipment();
        updateEntity(shipment, dto);
        shipment.setDoNumber("DO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Save shipment first to get ID
        GoodsShipment savedShipment = shipmentRepository.save(shipment);

        // CRITICAL: Stock reduction logic - Business Rule: "jika membuat DO maka stok
        // akan berkurang"
        if (dto.items() != null && !dto.items().isEmpty()) {
            for (var itemDTO : dto.items()) {
                // Find spare part
                SparePart part = sparePartRepository.findById(itemDTO.partId())
                        .orElseThrow(() -> new RuntimeException("Part not found: " + itemDTO.partId()));

                // Verify sufficient stock
                // Use long to prevent integer overflow during check
                long currentStock = part.getCurrentStock().longValue();
                long requestedQty = itemDTO.quantity().longValue();

                if (currentStock < requestedQty) {
                    throw new IllegalStateException(
                            String.format("Insufficient stock for part %s. Available: %d, Required: %d",
                                    part.getName(), currentStock, requestedQty));
                }

                // Create shipment item
                ShipmentItem item = new ShipmentItem();
                item.setShipment(savedShipment);
                item.setPart(part);
                item.setQuantity(itemDTO.quantity());
                item.setUnitPrice(itemDTO.unitPrice());
                item.setNotes(itemDTO.notes());
                savedShipment.getItems().add(item);

                // Reduce stock (Safe cast because we checked bounds)
                part.setCurrentStock((int) (currentStock - requestedQty));
                sparePartRepository.save(part);

                // Create inventory transaction record
                InventoryTransaction trx = new InventoryTransaction();
                trx.setTrxNumber(
                        "TRX-OUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                trx.setDate(LocalDate.now());
                trx.setType("TRANSFER_OUT");
                trx.setPart(part);
                trx.setQuantity(-itemDTO.quantity().intValue()); // Negative for OUT
                trx.setReferenceId(savedShipment.getDoNumber());
                trx.setNotes("Goods Shipment to " + dto.targetLocationName());
                inventoryTransactionRepository.save(trx);
            }
        }

        return mapToDTO(shipmentRepository.save(savedShipment));
    }

    @Override
    public GoodsShipmentDTO updateShipment(UUID id, GoodsShipmentDTO dto) {
        GoodsShipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        updateEntity(shipment, dto);
        return mapToDTO(shipmentRepository.save(shipment));
    }

    @Override
    public GoodsShipmentDTO updateShipmentStatus(UUID id, String status) {
        GoodsShipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        shipment.setStatus(status);
        return mapToDTO(shipmentRepository.save(shipment));
    }

    @Override
    public void deleteShipment(UUID id) {
        shipmentRepository.deleteById(id);
    }

    private void updateEntity(GoodsShipment s, GoodsShipmentDTO dto) {
        s.setDate(LocalDate.parse(dto.date()));

        if (dto.sourceLocationId() != null) {
            s.setSourceLocation(locationRepository.findById(dto.sourceLocationId()).orElse(null));
        }

        s.setTargetType(dto.targetType());

        if (dto.targetLocationId() != null) {
            s.setTargetLocation(locationRepository.findById(dto.targetLocationId()).orElse(null));
        }

        if (dto.targetSupplierId() != null) {
            s.setTargetSupplier(supplierRepository.findById(dto.targetSupplierId()).orElse(null));
        }

        s.setTransportProvider(dto.transportProvider());

        if (dto.driverEmployeeId() != null) {
            s.setDriverEmployee(employeeRepository.findById(dto.driverEmployeeId()).orElse(null));
        }

        if (dto.vehicleEquipmentId() != null) {
            s.setVehicleEquipment(equipmentRepository.findById(dto.vehicleEquipmentId()).orElse(null));
        }

        s.setExternalDriverName(dto.externalDriverName());
        s.setExternalVehicleDesc(dto.externalVehicleDesc());
        s.setPoliceNumber(dto.policeNumber());
        s.setStatus(dto.status());
        s.setNotes(dto.notes());
    }

    private GoodsShipmentDTO mapToDTO(GoodsShipment s) {
        return new GoodsShipmentDTO(
                s.getId(),
                s.getDoNumber(),
                s.getDate().toString(),
                s.getSourceLocation() != null ? s.getSourceLocation().getId() : null,
                s.getSourceLocation() != null ? s.getSourceLocation().getName() : null,
                s.getTargetType(),
                s.getTargetLocation() != null ? s.getTargetLocation().getId() : null,
                s.getTargetLocation() != null ? s.getTargetLocation().getName() : null,
                s.getTargetSupplier() != null ? s.getTargetSupplier().getId() : null,
                s.getTargetSupplier() != null ? s.getTargetSupplier().getName() : null,
                s.getTransportProvider(),
                s.getDriverEmployee() != null ? s.getDriverEmployee().getId() : null,
                s.getDriverEmployee() != null ? s.getDriverEmployee().getName() : null,
                s.getVehicleEquipment() != null ? s.getVehicleEquipment().getId() : null,
                s.getVehicleEquipment() != null ? s.getVehicleEquipment().getCode() : null,
                s.getExternalDriverName(),
                s.getExternalVehicleDesc(),
                s.getPoliceNumber(),
                s.getStatus(),
                s.getNotes(),
                s.getItems() != null ? s.getItems().stream()
                        .map(item -> new ShipmentItemDTO(
                                item.getId(),
                                item.getPart().getId(),
                                item.getPart().getPartNumber(),
                                item.getPart().getName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getNotes()))
                        .toList() : List.of());
    }
}
