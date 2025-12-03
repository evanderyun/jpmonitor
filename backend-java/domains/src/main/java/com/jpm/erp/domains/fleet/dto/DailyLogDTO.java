package com.jpm.erp.domains.fleet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record DailyLogDTO(
                UUID id,
                String logDate,
                UUID equipmentId,
                String equipmentCode,
                UUID operatorId,
                String operatorName,
                UUID locationId,
                String locationName,
                UUID projectId,
                String projectName,
                @JsonProperty("startHM") BigDecimal startHm,
                @JsonProperty("endHM") BigDecimal endHm,
                BigDecimal totalHours,
                String activityCode,
                String notes) {
}
