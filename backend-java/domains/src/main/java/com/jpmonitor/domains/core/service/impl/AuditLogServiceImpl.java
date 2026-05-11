package com.jpmonitor.domains.core.service.impl;

import com.jpmonitor.domains.core.dto.AuditLogDTO;
import com.jpmonitor.domains.core.entity.AuditLog;
import com.jpmonitor.domains.core.repository.AuditLogRepository;
import com.jpmonitor.domains.core.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void logAction(String action, String entityName, String entityId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDetails(details);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            log.setUsername(auth.getName());
            // Note: User ID would ideally be extracted from Principal if available
        } else {
            log.setUsername("SYSTEM");
        }

        auditLogRepository.save(log);
    }

    private AuditLogDTO mapToDTO(AuditLog log) {
        return new AuditLogDTO(
                log.getId(),
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getUserId(),
                log.getUsername(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt());
    }
}
