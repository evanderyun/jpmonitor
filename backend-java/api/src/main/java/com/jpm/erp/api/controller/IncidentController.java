package com.jpm.erp.api.controller;

import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.domains.hse.dto.IncidentDTO;
import com.jpm.erp.domains.hse.entity.Incident;
import com.jpm.erp.domains.hse.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hse")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final LocationRepository locationRepository;

    @GetMapping("/incidents")
    public List<IncidentDTO> getIncidents() {
        return incidentRepository.findAll().stream()
                .map(i -> new IncidentDTO(
                        i.getId(),
                        i.getDate().toString(),
                        i.getTime() != null ? i.getTime().toString() : null,
                        i.getType(),
                        i.getSeverity(),
                        i.getLocation() != null ? i.getLocation().getId() : null,
                        i.getLocation() != null ? i.getLocation().getName() : "Unknown",
                        i.getProject() != null ? i.getProject().getId() : null,
                        i.getProject() != null ? i.getProject().getName() : "Unknown",
                        i.getLocationDetail(),
                        i.getDescription(),
                        i.getImmediateAction(),
                        i.getStatus(),
                        i.getReportedBy() != null ? i.getReportedBy().getId() : null,
                        i.getReportedBy() != null ? i.getReportedBy().getName() : null))
                .collect(Collectors.toList());
    }

    @PostMapping("/incidents")
    public ResponseEntity<IncidentDTO> reportIncident(@RequestBody IncidentDTO dto) {
        Incident inc = new Incident();
        inc.setDate(LocalDate.parse(dto.date()));
        inc.setType(dto.type());
        inc.setDescription(dto.description());
        inc.setLocationDetail(dto.locationDetail());
        inc.setStatus(dto.status() != null ? dto.status() : "OPEN");

        if (dto.locationId() != null) {
            inc.setLocation(locationRepository.findById(dto.locationId()).orElse(null));
        }

        Incident saved = incidentRepository.save(inc);
        return ResponseEntity.ok(new IncidentDTO(
                saved.getId(),
                saved.getDate().toString(),
                saved.getTime() != null ? saved.getTime().toString() : null,
                saved.getType(),
                saved.getSeverity(),
                saved.getLocation() != null ? saved.getLocation().getId() : null,
                saved.getLocation() != null ? saved.getLocation().getName() : null,
                saved.getProject() != null ? saved.getProject().getId() : null,
                saved.getProject() != null ? saved.getProject().getName() : null,
                saved.getLocationDetail(),
                saved.getDescription(),
                saved.getImmediateAction(),
                saved.getStatus(),
                saved.getReportedBy() != null ? saved.getReportedBy().getId() : null,
                saved.getReportedBy() != null ? saved.getReportedBy().getName() : null));
    }
}