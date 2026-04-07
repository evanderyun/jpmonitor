package com.jpm.erp.api.controller;
import com.jpm.erp.domains.hse.dto.IncidentDTO;
import com.jpm.erp.domains.hse.service.HseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hse/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final HseService hseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public List<IncidentDTO> getAllIncidents() { return hseService.getAllIncidents(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<IncidentDTO> reportIncident(@RequestBody IncidentDTO dto) { return ResponseEntity.ok(hseService.createIncident(dto)); }
}
