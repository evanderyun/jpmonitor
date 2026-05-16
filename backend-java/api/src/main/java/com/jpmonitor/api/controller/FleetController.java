package com.jpmonitor.api.controller;

import com.jpmonitor.domains.fleet.dto.EquipmentDTO;
import com.jpmonitor.domains.fleet.dto.MaintenanceRecordDTO;
import com.jpmonitor.domains.fleet.service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;

    // ============================================================================
    // EQUIPMENT ENDPOINTS
    // ============================================================================

    @GetMapping({ "/equipment", "/fleet" })
    public List<EquipmentDTO> getAllEquipment(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID locationId) {
        return fleetService.getAllEquipment(status, type, locationId);
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> getEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok(fleetService.getEquipment(id));
    }

    @PostMapping("/equipment")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentDTO dto) {
        return ResponseEntity.ok(fleetService.createEquipment(dto));
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable UUID id, @Valid @RequestBody EquipmentDTO dto) {
        return ResponseEntity.ok(fleetService.updateEquipment(id, dto));
    }

    @PutMapping("/equipment/{id}/location")
    public ResponseEntity<EquipmentDTO> updateEquipmentLocation(
            @PathVariable UUID id,
            @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(fleetService.updateEquipmentLocation(id, request.locationId()));
    }

    @PutMapping("/equipment/{id}/status")
    public ResponseEntity<EquipmentDTO> updateEquipmentStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(fleetService.updateEquipmentStatus(id, request.status()));
    }

    @PutMapping("/equipment/{id}/hourmeter")
    public ResponseEntity<EquipmentDTO> updateEquipmentHourMeter(
            @PathVariable UUID id,
            @RequestBody HourMeterUpdateRequest request) {
        return ResponseEntity.ok(fleetService.updateEquipmentHourMeter(id, request.hourMeter()));
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable UUID id) {
        fleetService.deleteEquipment(id);
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
        return fleetService.getAllMaintenanceRecords(equipmentId, status, type);
    }

    @GetMapping("/maintenance/{id}")
    public ResponseEntity<MaintenanceRecordDTO> getMaintenanceRecord(@PathVariable UUID id) {
        return ResponseEntity.ok(fleetService.getMaintenanceRecord(id));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceRecordDTO> createMaintenance(@Valid @RequestBody MaintenanceRecordDTO dto) {
        return ResponseEntity.ok(fleetService.createMaintenance(dto));
    }

    @PutMapping("/maintenance/{id}")
    public ResponseEntity<MaintenanceRecordDTO> updateMaintenance(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceRecordDTO dto) {
        return ResponseEntity.ok(fleetService.updateMaintenance(id, dto));
    }

    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable UUID id) {
        fleetService.deleteMaintenance(id);
        return ResponseEntity.ok().build();
    }

    // ============================================================================
    // Request DTOs for partial updates
    // ============================================================================

    public record LocationUpdateRequest(UUID locationId) {
    }

    public record StatusUpdateRequest(String status) {
    }

    public record HourMeterUpdateRequest(BigDecimal hourMeter) {
    }
}
