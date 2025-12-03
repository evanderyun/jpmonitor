package com.jpm.erp.domains.core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogDTO(
        UUID id,
        String action,
        String entityName,
        String entityId,
        UUID userId,
        String username,
        String details,
        String ipAddress,
        LocalDateTime createdAt) {
}
