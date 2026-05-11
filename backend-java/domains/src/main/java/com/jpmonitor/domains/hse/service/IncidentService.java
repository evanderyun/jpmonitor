package com.jpmonitor.domains.hse.service;

import com.jpmonitor.domains.hse.dto.IncidentDTO;
import java.util.List;
import java.util.UUID;

public interface IncidentService {
    List<IncidentDTO> getAllIncidents();

    IncidentDTO createIncident(IncidentDTO dto);

    IncidentDTO updateIncident(UUID id, IncidentDTO dto);

    void deleteIncident(UUID id);
}
