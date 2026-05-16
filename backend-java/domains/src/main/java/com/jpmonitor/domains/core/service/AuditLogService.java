package com.jpmonitor.domains.core.service;

import com.jpmonitor.domains.core.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {
    List<AuditLogDTO> getAllLogs();

    Page<AuditLogDTO> getLogs(String module, Pageable pageable);

    void logAction(String action, String entityName, String entityId, String details);
}
