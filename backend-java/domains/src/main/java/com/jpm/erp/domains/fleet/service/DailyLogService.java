package com.jpm.erp.domains.fleet.service;

import com.jpm.erp.domains.fleet.dto.DailyLogDTO;
import java.util.List;
import java.util.UUID;

public interface DailyLogService {
    List<DailyLogDTO> getAllLogs();

    DailyLogDTO createLog(DailyLogDTO dto);

    DailyLogDTO updateLog(UUID id, DailyLogDTO dto);

    void deleteLog(UUID id);
}
