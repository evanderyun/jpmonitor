package com.jpm.erp.domains.analytics.service.impl;

import com.jpm.erp.domains.analytics.dto.DashboardStatsDTO;
import com.jpm.erp.domains.analytics.service.DashboardService;
import com.jpm.erp.domains.fleet.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EquipmentRepository equipmentRepository;

    @Override
    public DashboardStatsDTO getStats() {
        // Mock implementation for now - aggregation logic to be added
        return new DashboardStatsDTO(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                (int) equipmentRepository.count(),
                0,
                new HashMap<>(),
                new HashMap<>());
    }
}
