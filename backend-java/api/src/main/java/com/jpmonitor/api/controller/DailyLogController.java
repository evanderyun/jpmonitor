package com.jpmonitor.api.controller;

import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.fleet.dto.DailyLogDTO;
import com.jpmonitor.domains.fleet.entity.DailyLog;
import com.jpmonitor.domains.fleet.entity.Equipment;
import com.jpmonitor.domains.fleet.repository.DailyLogRepository;
import com.jpmonitor.domains.fleet.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dailylogs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogRepository dailyLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    public List<DailyLogDTO> getAll() {
        return dailyLogRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<DailyLogDTO> create(@RequestBody DailyLogDTO dto) {
        DailyLog log = new DailyLog();
        log.setLogDate(LocalDate.parse(dto.logDate())); // ✅ Fixed: date() → logDate()

        Equipment eq = equipmentRepository.findById(dto.equipmentId()).orElseThrow();
        log.setEquipment(eq);

        log.setStartHm(dto.startHm()); // ✅ Fixed: startHM() → startHm()
        log.setEndHm(dto.endHm()); // ✅ Fixed: endHM() → endHm()
        log.setOperatorName(dto.operatorName());
        log.setActivityCode(dto.activityCode()); // ✅ Fixed: activity() → activityCode()
        log.setNotes(dto.notes());

        if (dto.locationId() != null) {
            log.setLocation(locationRepository.findById(dto.locationId()).orElse(null));
        }

        DailyLog saved = dailyLogRepository.save(log);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    private DailyLogDTO mapToDTO(DailyLog l) {
        return new DailyLogDTO(
                l.getId(),
                l.getLogDate().toString(),
                l.getEquipment().getId(),
                l.getEquipment().getCode(),
                l.getOperator() != null ? l.getOperator().getId() : null,
                l.getOperatorName(),
                l.getLocation() != null ? l.getLocation().getId() : null,
                l.getLocation() != null ? l.getLocation().getName() : "Unknown",
                l.getProject() != null ? l.getProject().getId() : null,
                l.getProject() != null ? l.getProject().getName() : "Unknown",
                l.getStartHm(),
                l.getEndHm(),
                l.getTotalHours(),
                l.getActivityCode(),
                l.getNotes());
    }
}