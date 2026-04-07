package com.jpm.erp.api.controller;
import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.domains.fleet.dto.DailyLogDTO;
import com.jpm.erp.domains.fleet.entity.DailyLog;
import com.jpm.erp.domains.fleet.entity.Equipment;
import com.jpm.erp.domains.fleet.repository.DailyLogRepository;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.core.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dailylogs")
@RequiredArgsConstructor
public class DailyLogController {
    private final DailyLogRepository dailyLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<DailyLogDTO> getAllDailyLogs() {
        return dailyLogRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<DailyLogDTO> createDailyLog(@Valid @RequestBody DailyLogDTO dto) {
        DailyLog log = new DailyLog();
        log.setWorkingDate(java.time.LocalDate.parse(dto.workingDate()));
        log.setHourMeterStart(dto.hourMeterStart());
        log.setHourMeterEnd(dto.hourMeterEnd());
        if (dto.equipmentCode() != null) {
            Equipment eq = equipmentRepository.findByEquipmentCode(dto.equipmentCode()).orElse(null);
            log.setEquipment(eq);
        }
        if (dto.operatorId() != null) {
            log.setOperator(employeeRepository.findById(dto.operatorId()).orElse(null));
        }
        log.setLocationName(dto.locationName());
        log.setActivityDescription(dto.activityDescription());
        log.setFuelConsumed(dto.fuelConsumed());
        DailyLog saved = dailyLogRepository.save(log);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    private DailyLogDTO mapToDTO(DailyLog dl) {
        return new DailyLogDTO(dl.getId(), dl.getWorkingDate() != null ? dl.getWorkingDate().toString() : null,
                dl.getEquipment() != null ? dl.getEquipment().getEquipmentCode() : null,
                dl.getOperator() != null ? dl.getOperator().getId() : null,
                dl.getOperator() != null ? dl.getOperator().getFullName() : null,
                dl.getLocationName(), dl.getHourMeterStart(), dl.getHourMeterEnd(),
                dl.getActivityDescription(), dl.getFuelConsumed());
    }
}
