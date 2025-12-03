package com.jpm.erp.domains.core.service;

import com.jpm.erp.domains.core.dto.AuditLogDTO;
import java.util.List;

public interface AuditLogService {
    List<AuditLogDTO> getAllLogs();

    void logAction(String action, String entityName, String entityId, String details);
}
