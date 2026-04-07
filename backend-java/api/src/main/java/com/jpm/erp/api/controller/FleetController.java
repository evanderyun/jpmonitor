package com.jpm.erp.api.controller;
import com.jpm.erp.domains.fleet.dto.EquipmentDTO;
import com.jpm.erp.domains.fleet.dto.MaintenanceRecordDTO;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.entity.MaintenanceRecord;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.fleet.repository.MaintenanceRecordRepository;
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
@RequiredArgsConstructor
public class FleetController {
    private final EquipmentRepository equipmentRepository;
    private final MaintenanceRecordRepository maintenanceRepository;

    @GetMapping("/api/equipment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<EquipmentDTO> getAllEquipment(@RequestParam(required = false) String status, @RequestParam(required = false) String type, @RequestParam(required = false) UUID locationId) {
        return equipmentRepository.findAll().stream()
                .filter(e -> status == null || status.equals(e.getStatus()))
                .filter(e -> type == null || type.equals(e.getType()))
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @PostMapping("/api/equipment")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentDTO dto) {
        Equipment eq = new Equipment();
        updateEquipmentFromDTO(eq, dto);
        return ResponseEntity.ok(mapToDTO(equipmentRepository.save(eq)));
    }

    @PutMapping("/api/equipment/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable UUID id, @Valid @RequestBody EquipmentDTO dto) {
        Equipment eq = equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        updateEquipmentFromDTO(eq, dto);
        return ResponseEntity.ok(mapToDTO(equipmentRepository.save(eq)));
    }

    @DeleteMapping("/api/equipment/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable UUID id) {
        if (!equipmentRepository.existsById(id)) throw new ResourceNotFoundException("Equipment", id);
        equipmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/equipment/{id}/location")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateEquipmentLocation(@PathVariable UUID id, @RequestBody LocationUpdateRequest req) {
        Equipment eq = equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        eq.setLocationName(req.locationName());
        equipmentRepository.save(eq);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/equipment/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateEquipmentStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest req) {
        Equipment eq = equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        eq.setStatus(req.status());
        equipmentRepository.save(eq);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/equipment/{id}/hourmeter")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<Void> updateHourMeter(@PathVariable UUID id, @RequestBody HourMeterUpdate req) {
        Equipment eq = equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        eq.setHourMeter(req.hourMeter());
        equipmentRepository.save(eq);
        return ResponseEntity.ok().build();
    }

    // Maintenance endpoints
    @GetMapping("/api/maintenance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public List<MaintenanceRecordDTO> getAllMaintenance() {
        return maintenanceRepository.findAll().stream().map(this::mapMaintToDTO).collect(Collectors.toList());
    }

    @PostMapping("/api/maintenance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<MaintenanceRecordDTO> createMaintenance(@Valid @RequestBody MaintenanceRecordDTO dto) {
        return ResponseEntity.ok(mapMaintToDTO(maintenanceRepository.save(new MaintenanceRecord())));
    }

    @PutMapping("/api/maintenance/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MaintenanceRecordDTO> updateMaintenance(@PathVariable UUID id, @Valid @RequestBody MaintenanceRecordDTO dto) {
        MaintenanceRecord rec = maintenanceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Maintenance Record", id));
        return ResponseEntity.ok(mapMaintToDTO(maintenanceRepository.save(rec)));
    }

    @DeleteMapping("/api/maintenance/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable UUID id) {
        if (!maintenanceRepository.existsById(id)) throw new ResourceNotFoundException("Maintenance Record", id);
        maintenanceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void updateEquipmentFromDTO(Equipment eq, EquipmentDTO dto) {
        eq.setEquipmentCode(dto.equipmentCode());
        eq.setType(dto.type());
        eq.setMake(dto.make());
        eq.setModel(dto.model());
        eq.setYear(dto.year());
        eq.setStatus(dto.status());
        eq.setHourMeter(dto.hourMeter());
        eq.setLocationName(dto.locationName());
    }

    private EquipmentDTO mapToDTO(Equipment e) {
        return new EquipmentDTO(e.getId(), e.getEquipmentCode(), e.getType(), e.getMake(), e.getModel(), e.getYear(), e.getStatus(), e.getHourMeter(), e.getLocationName());
    }

    private MaintenanceRecordDTO mapMaintToDTO(MaintenanceRecord m) {
        return new MaintenanceRecordDTO(m.getId(), m.getEquipment() != null ? m.getEquipment().getId() : null, m.getMaintenanceType(), m.getDescription(), m.getDatePerformed() != null ? m.getDatePerformed().toString() : null, m.getHourMeter(), m.getCost(), m.getPerformedBy(), m.getNextServiceDue() != null ? m.getNextServiceDue().toString() : null);
    }

    private record LocationUpdateRequest(String locationName) {}
    private record StatusUpdateRequest(String status) {}
    private record HourMeterUpdate(Double hourMeter) {}
}
