package com.jpm.erp.domains.hse.dto;

import java.util.UUID;

public record IncidentDTO(
        UUID id,
        String date,
        String time, // ✅ Added for incident time tracking
        String type,
        String severity, // ✅ Added for severity level
        UUID locationId,
        String locationName,
        UUID projectId, // ✅ Added for project association
        String projectName, // ✅ Added for frontend display
        String locationDetail,
        String description,
        String immediateAction, // ✅ Added for action taken
        String status,
        UUID reportedById, // ✅ Added for reporter tracking
        String reportedByName // ✅ Added for frontend display
) {
}