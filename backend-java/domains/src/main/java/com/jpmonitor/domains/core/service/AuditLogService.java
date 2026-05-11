package com.jpmonitor.domains.core.service;

import com.jpmonitor.domains.core.dto.AuditLogDTO;
import java.util.List;

public interface AuditLogService {
    List<AuditLogDTO> getAllLogs();

    void logAction(String action, String entityName, String entityId, String details);
}
