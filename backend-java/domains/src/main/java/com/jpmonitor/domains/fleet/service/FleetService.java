package com.jpmonitor.domains.fleet.service;

import com.jpmonitor.domains.fleet.dto.EquipmentDTO;
import com.jpmonitor.domains.fleet.dto.MaintenanceRecordDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface FleetService {

    // ==================== EQUIPMENT ====================

    List<EquipmentDTO> getAllEquipment(String status, String type, UUID locationId);

    EquipmentDTO getEquipment(UUID id);

    EquipmentDTO createEquipment(EquipmentDTO dto);

    EquipmentDTO updateEquipment(UUID id, EquipmentDTO dto);

    EquipmentDTO updateEquipmentLocation(UUID id, UUID locationId);

    EquipmentDTO updateEquipmentStatus(UUID id, String status);

    EquipmentDTO updateEquipmentHourMeter(UUID id, BigDecimal hourMeter);

    void deleteEquipment(UUID id);

    // ==================== MAINTENANCE ====================

    List<MaintenanceRecordDTO> getAllMaintenanceRecords(UUID equipmentId, String status, String type);

    MaintenanceRecordDTO getMaintenanceRecord(UUID id);

    MaintenanceRecordDTO createMaintenance(MaintenanceRecordDTO dto);

    MaintenanceRecordDTO updateMaintenance(UUID id, MaintenanceRecordDTO dto);

    void deleteMaintenance(UUID id);
}
