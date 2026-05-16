package com.jpmonitor.domains.fleet.service.impl;

import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.fleet.dto.EquipmentDTO;
import com.jpmonitor.domains.fleet.dto.MaintenanceRecordDTO;
import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.fleet.entity.MaintenanceRecord;
import com.jpmonitor.domains.fleet.entity.WorkOrder;
import com.jpmonitor.domains.fleet.repository.EquipmentRepository;
import com.jpmonitor.domains.fleet.repository.MaintenanceRecordRepository;
import com.jpmonitor.domains.fleet.repository.WorkOrderRepository;
import com.jpmonitor.domains.fleet.service.FleetService;
import com.jpmonitor.platform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FleetServiceImpl implements FleetService {

    private final EquipmentRepository equipmentRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LocationRepository locationRepository;

    // ==================== EQUIPMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getAllEquipment(String status, String type, UUID locationId) {
        return equipmentRepository.findAll().stream()
                .filter(e -> status == null || status.equals(e.getStatus()))
                .filter(e -> type == null || type.equals(e.getType()))
                .filter(e -> locationId == null
                        || (e.getLocation() != null && locationId.equals(e.getLocation().getId())))
                .map(this::mapEquipmentToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDTO getEquipment(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        return mapEquipmentToDTO(equipment);
    }

    @Override
    public EquipmentDTO createEquipment(EquipmentDTO dto) {
        Equipment equipment = new Equipment();
        updateEquipmentFromDTO(equipment, dto);
        Equipment saved = equipmentRepository.save(equipment);
        return mapEquipmentToDTO(saved);
    }

    @Override
    public EquipmentDTO updateEquipment(UUID id, EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        updateEquipmentFromDTO(equipment, dto);
        Equipment saved = equipmentRepository.save(equipment);
        return mapEquipmentToDTO(saved);
    }

    @Override
    public EquipmentDTO updateEquipmentLocation(UUID id, UUID locationId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        if (locationId != null) {
            equipment.setLocation(locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location", locationId)));
        } else {
            equipment.setLocation(null);
        }
        Equipment saved = equipmentRepository.save(equipment);
        return mapEquipmentToDTO(saved);
    }

    @Override
    public EquipmentDTO updateEquipmentStatus(UUID id, String status) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        equipment.setStatus(status);
        Equipment saved = equipmentRepository.save(equipment);
        return mapEquipmentToDTO(saved);
    }

    @Override
    public EquipmentDTO updateEquipmentHourMeter(UUID id, BigDecimal hourMeter) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        equipment.setHourMeter(hourMeter);
        Equipment saved = equipmentRepository.save(equipment);
        return mapEquipmentToDTO(saved);
    }

    @Override
    public void deleteEquipment(UUID id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment", id);
        }
        equipmentRepository.deleteById(id);
    }

    // ==================== MAINTENANCE ====================

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordDTO> getAllMaintenanceRecords(UUID equipmentId, String status, String type) {
        return maintenanceRepository.findAll().stream()
                .filter(r -> equipmentId == null || equipmentId.equals(r.getEquipment().getId()))
                .filter(r -> status == null
                        || (r.getWorkOrder() != null && status.equals(r.getWorkOrder().getStatus())))
                .filter(r -> type == null || (r.getWorkOrder() != null && type.equals(r.getWorkOrder().getType())))
                .map(this::mapMaintToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRecordDTO getMaintenanceRecord(UUID id) {
        MaintenanceRecord record = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance Record", id));
        return mapMaintToDTO(record);
    }

    @Override
    public MaintenanceRecordDTO createMaintenance(MaintenanceRecordDTO dto) {
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
        return mapMaintToDTO(saved);
    }

    @Override
    public MaintenanceRecordDTO updateMaintenance(UUID id, MaintenanceRecordDTO dto) {
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
        return mapMaintToDTO(saved);
    }

    @Override
    public void deleteMaintenance(UUID id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance Record", id);
        }
        maintenanceRepository.deleteById(id);
    }

    // ==================== HELPER METHODS ====================

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
}
