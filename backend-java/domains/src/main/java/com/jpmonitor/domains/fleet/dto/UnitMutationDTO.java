package com.jpmonitor.domains.fleet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record UnitMutationDTO(
                UUID id,
                String mutationNumber,
                String type,
                UUID equipmentId,
                String equipmentCode,
                UUID sourceLocationId,
                String sourceLocationName,
                UUID targetLocationId,
                String targetLocationName,
                String departureDate,
                String arrivalDate,
                @JsonProperty("mutationHM") BigDecimal mutationHm,
                String notes,
                String driverName,
                String transportUnit,
                String transportPolNumber,
                String senderCompany,
                String senderName,
                String recipientCompany,
                String recipientName) {
}
