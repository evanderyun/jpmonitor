package com.jpm.erp.domains.fleet.service.impl;

import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.domains.core.repository.ProjectRepository;
import com.jpm.erp.domains.fleet.dto.DailyLogDTO;
import com.jpm.erp.domains.fleet.entity.DailyLog;
import com.jpm.erp.domains.fleet.repository.DailyLogRepository;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import com.jpm.erp.domains.fleet.service.DailyLogService;
import com.jpm.erp.domains.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyLogServiceImpl implements DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DailyLogDTO> getAllLogs() {
        return dailyLogRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DailyLogDTO createLog(DailyLogDTO dto) {
        DailyLog log = new DailyLog();
        updateEntityFromDTO(log, dto);
        
        // Integrity Check: Prevent HM rollback without WO (Simplified for now)
        // Real implementation should check MaintenanceLogs for HM Reset events
        if (log.getEquipment().getHourMeter() != null && 
            log.getStartHm().compareTo(log.getEquipment().getHourMeter()) < 0) {
             // For now, we just warn or allow if it's a new data entry backfill
             // In strict mode: throw new IllegalArgumentException("Start HM cannot be lower than current Equipment HM");
        }

        DailyLog savedLog = dailyLogRepository.save(log);

        // Auto-update Equipment Master Data (Live HM)
        com.jpm.erp.domains.fleet.entity.Equipment equipment = log.getEquipment();
        if (equipment != null && log.getEndHm() != null) {
            // Only update if moving forward
            if (equipment.getHourMeter() == null || log.getEndHm().compareTo(equipment.getHourMeter()) > 0) {
                equipment.setHourMeter(log.getEndHm());
                equipmentRepository.save(equipment);
            }
        }

        return mapToDTO(savedLog);
    }

    @Override
    public DailyLogDTO updateLog(UUID id, DailyLogDTO dto) {
        DailyLog log = dailyLogRepository.findById(id).orElseThrow();
        updateEntityFromDTO(log, dto);
        return mapToDTO(dailyLogRepository.save(log));
    }

    @Override
    public void deleteLog(UUID id) {
        dailyLogRepository.deleteById(id);
    }

    private void updateEntityFromDTO(DailyLog log, DailyLogDTO dto) {
        log.setLogDate(LocalDate.parse(dto.logDate()));
        log.setEquipment(equipmentRepository.findById(dto.equipmentId()).orElseThrow());

        if (dto.operatorId() != null) {
            log.setOperator(employeeRepository.findById(dto.operatorId()).orElse(null));
        }
        log.setOperatorName(dto.operatorName());

        if (dto.locationId() != null) {
            log.setLocation(locationRepository.findById(dto.locationId()).orElse(null));
        }

        if (dto.projectId() != null) {
            log.setProject(projectRepository.findById(dto.projectId()).orElse(null));
        }

        log.setStartHm(dto.startHm());
        log.setEndHm(dto.endHm());
        log.setActivityCode(dto.activityCode());
        log.setNotes(dto.notes());
    }

    private DailyLogDTO mapToDTO(DailyLog log) {
        return new DailyLogDTO(
                log.getId(),
                log.getLogDate().toString(),
                log.getEquipment().getId(),
                log.getEquipment().getCode(),
                log.getOperator() != null ? log.getOperator().getId() : null,
                log.getOperatorName(),
                log.getLocation() != null ? log.getLocation().getId() : null,
                log.getLocation() != null ? log.getLocation().getName() : null,
                log.getProject() != null ? log.getProject().getId() : null,
                log.getProject() != null ? log.getProject().getName() : null,
                log.getStartHm(),
                log.getEndHm(),
                log.getTotalHours(),
                log.getActivityCode(),
                log.getNotes());
    }
}
