package com.jpm.erp.api.controller;

import com.jpm.erp.domains.fleet.dto.EquipmentDTO;
import com.jpm.erp.domains.fleet.dto.MaintenanceRecordDTO;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.entity.MaintenanceRecord;
import com.jpm.erp.domains.fleet.entity.WorkOrder;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.fleet.repository.MaintenanceRecordRepository;
import com.jpm.erp.domains.fleet.repository.WorkOrderRepository;
import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // Keep for backward compatibility, but specific paths in methods
@RequiredArgsConstructor
public class FleetController {

    private final EquipmentRepository equipmentRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LocationRepository locationRepository;

    // ============================================================================
    // EQUIPMENT ENDPOINTS
    // ============================================================================

    @GetMapping({ "/equipment", "/fleet" })
    public List<EquipmentDTO> getAllEquipment(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID locationId) {

        List<Equipment> equipment = equipmentRepository.findAll();

        // Apply filters
        return equipment.stream()
                .filter(e -> status == null || status.equals(e.getStatus()))
                .filter(e -> type == null || type.equals(e.getType()))
                .filter(e -> locationId == null
                        || (e.getLocation() != null && locationId.equals(e.getLocation().getId())))
                .map(this::mapEquipmentToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> getEquipment(@PathVariable UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        return ResponseEntity.ok(mapEquipmentToDTO(equipment));
    }

    @PostMapping("/equipment")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentDTO dto) {
        Equipment equipment = new Equipment();
        updateEquipmentFromDTO(equipment, dto);

        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(mapEquipmentToDTO(saved));
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable UUID id, @Valid @RequestBody EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        updateEquipmentFromDTO(equipment, dto);
        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(mapEquipmentToDTO(saved));
    }

    @PutMapping("/equipment/{id}/location")
    public ResponseEntity<EquipmentDTO> updateEquipmentLocation(
            @PathVariable UUID id,
            @RequestBody LocationUpdateRequest request) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        if (request.locationId() != null) {
            equipment.setLocation(locationRepository.findById(request.locationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", request.locationId())));
        } else {
            equipment.setLocation(null);
        }

        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(mapEquipmentToDTO(saved));
    }

    @PutMapping("/equipment/{id}/status")
    public ResponseEntity<EquipmentDTO> updateEquipmentStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        equipment.setStatus(request.status());
        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(mapEquipmentToDTO(saved));
    }

    @PutMapping("/equipment/{id}/hourmeter")
    public ResponseEntity<EquipmentDTO> updateEquipmentHourMeter(
            @PathVariable UUID id,
            @RequestBody HourMeterUpdateRequest request) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        equipment.setHourMeter(request.hourMeter());
        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.ok(mapEquipmentToDTO(saved));
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable UUID id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment", id);
        }
        equipmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ============================================================================
    // MAINTENANCE ENDPOINTS
    // ============================================================================

    @GetMapping("/maintenance")
    public List<MaintenanceRecordDTO> getAllMaintenanceRecords(
            @RequestParam(required = false) UUID equipmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        List<MaintenanceRecord> records = maintenanceRepository.findAll();

        // Apply filters
        return records.stream()
                .filter(r -> equipmentId == null || equipmentId.equals(r.getEquipment().getId()))
                .filter(r -> status == null
                        || (r.getWorkOrder() != null && status.equals(r.getWorkOrder().getStatus())))
                .filter(r -> type == null || (r.getWorkOrder() != null && type.equals(r.getWorkOrder().getType())))
                .map(this::mapMaintToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/maintenance/{id}")
    public ResponseEntity<MaintenanceRecordDTO> getMaintenanceRecord(@PathVariable UUID id) {
        MaintenanceRecord record = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance Record", id));
        return ResponseEntity.ok(mapMaintToDTO(record));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceRecordDTO> createMaintenance(@Valid @RequestBody MaintenanceRecordDTO dto) {
        // 1. Create Work Order (Simplified)
        WorkOrder wo = new WorkOrder();
        wo.setWoNumber("WO-" + System.currentTimeMillis());
        wo.setStatus(dto.status() != null ? dto.status() : "OPEN");
        wo.setType(dto.type());
        wo.setDescription(dto.description());

        Equipment eq = equipmentRepository.findById(dto.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", dto.equipmentId()));
        wo.setEquipment(eq);
        workOrderRepository.save(wo);

        // 2. Create Maintenance Record
        MaintenanceRecord rec = new MaintenanceRecord();
        rec.setWorkOrder(wo);
        rec.setEquipment(eq);
        rec.setServiceDate(LocalDate.parse(dto.startDate()));
        rec.setDescription(dto.description());

        // Map Costs from Frontend DTO to DB Columns
        rec.setPartsCost(dto.partsCost());
        rec.setTransportFuelCost(dto.mechanicStoringCost()); // Mapping confirmed
        rec.setMealAllowanceCost(dto.mechanicMealCost());
        rec.setDriverLaborCost(dto.driverStoringCost());
        rec.setExternalServiceCost(dto.externalCost());
        rec.setExternalInvoiceNumber(dto.externalInvoiceNumber());

        MaintenanceRecord saved = maintenanceRepository.save(rec);
        return ResponseEntity.ok(mapMaintToDTO(saved));
    }

    @PutMapping("/maintenance/{id}")
    public ResponseEntity<MaintenanceRecordDTO> updateMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceRecordDTO dto) {

        MaintenanceRecord record = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance Record", id));

        // Update fields
        if (dto.description() != null) {
            record.setDescription(dto.description());
        }
        if (dto.partsCost() != null) {
            record.setPartsCost(dto.partsCost());
        }
        if (dto.mechanicStoringCost() != null) {
            record.setTransportFuelCost(dto.mechanicStoringCost());
        }
        if (dto.mechanicMealCost() != null) {
            record.setMealAllowanceCost(dto.mechanicMealCost());
        }
        if (dto.driverStoringCost() != null) {
            record.setDriverLaborCost(dto.driverStoringCost());
        }
        if (dto.externalCost() != null) {
            record.setExternalServiceCost(dto.externalCost());
        }
        if (dto.externalInvoiceNumber() != null) {
            record.setExternalInvoiceNumber(dto.externalInvoiceNumber());
        }

        // Update work order status if provided
        if (dto.status() != null && record.getWorkOrder() != null) {
            record.getWorkOrder().setStatus(dto.status());
            workOrderRepository.save(record.getWorkOrder());
        }

        MaintenanceRecord saved = maintenanceRepository.save(record);
        return ResponseEntity.ok(mapMaintToDTO(saved));
    }

    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable UUID id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance Record", id);
        }
        maintenanceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ============================================================================
    // HELPER METHODS & DTOs
    // ============================================================================

    private void updateEquipmentFromDTO(Equipment equipment, EquipmentDTO dto) {
        equipment.setCode(dto.code());
        equipment.setName(dto.name() != null ? dto.name() : dto.code());
        equipment.setModel(dto.model());
        equipment.setType(dto.type());
        equipment.setBrand(dto.brand());
        equipment.setSerialNumber(dto.serialNumber());
        equipment.setEngineNumber(dto.engineNumber());
        equipment.setChassisNumber(dto.chassisNumber());
        equipment.setPlateNumber(dto.plateNumber());
        equipment.setManufactureYear(dto.manufactureYear());
        equipment.setStatus(dto.status() != null ? dto.status() : "Operational");
        equipment.setHourMeter(dto.hourMeter() != null ? dto.hourMeter() : BigDecimal.ZERO);
        equipment.setKilometer(dto.kilometer());
        equipment.setOwner(dto.owner());

        // Handle location if locationId is provided
        if (dto.locationId() != null) {
            equipment.setLocation(locationRepository.findById(dto.locationId()).orElse(null));
        }
    }

    private EquipmentDTO mapEquipmentToDTO(Equipment e) {
        return new EquipmentDTO(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getModel(),
                e.getType(),
                e.getBrand(),
                e.getSerialNumber(),
                e.getEngineNumber(),
                e.getChassisNumber(),
                e.getPlateNumber(),
                e.getManufactureYear(),
                e.getStatus(),
                e.getLocation() != null ? e.getLocation().getId() : null,
                e.getLocation() != null ? e.getLocation().getName() : "Unknown",
                e.getHourMeter(),
                e.getKilometer(),
                e.getOwner());
    }

    private MaintenanceRecordDTO mapMaintToDTO(MaintenanceRecord r) {
        return new MaintenanceRecordDTO(
                r.getId(),
                r.getEquipment().getId(),
                r.getWorkOrder() != null ? r.getWorkOrder().getWoNumber() : "-",
                r.getServiceDate().toString(),
                r.getWorkOrder() != null ? r.getWorkOrder().getType() : "Corrective",
                r.getDescription(),
                r.getWorkOrder() != null ? r.getWorkOrder().getStatus() : "CLOSED",
                r.getExternalServiceCost() != null && r.getExternalServiceCost().doubleValue() > 0 ? "EXTERNAL"
                        : "INTERNAL",
                r.getPartsCost(),
                r.getTransportFuelCost(),
                r.getMealAllowanceCost(),
                r.getDriverLaborCost(),
                r.getExternalServiceCost(),
                r.getExternalInvoiceNumber());
    }

    // Request DTOs for partial updates
    public record LocationUpdateRequest(UUID locationId) {
    }

    public record StatusUpdateRequest(String status) {
    }

    public record HourMeterUpdateRequest(BigDecimal hourMeter) {
    }
}
