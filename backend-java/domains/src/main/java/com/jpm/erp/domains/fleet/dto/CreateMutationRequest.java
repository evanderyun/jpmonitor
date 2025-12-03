package com.jpm.erp.domains.fleet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMutationRequest(
        String type, // ACQUISITION, TRANSFER, DISPOSAL
        UUID equipmentId,
        String equipmentCode,
        UUID sourceLocationId,
        String sourceLocation,
        UUID targetLocationId,
        String targetLocation,
        String departureDate,
        String arrivalDate,
        BigDecimal mutationHM,
        String referenceDocument,
        BigDecimal value,
        String notes,
        String driverName,
        String transportUnit,
        String transportPolNumber,
        String senderCompany,
        String senderName,
        String recipientCompany,
        String recipientName,
        String performedBy,
        NewUnitDetails newUnitDetails) {
    public record NewUnitDetails(
            String code,
            String model,
            String type,
            BigDecimal hourMeter,
            Integer newManufactureYear,
            BigDecimal newKilometer,
            String newOwner,
            String newChassisNumber,
            String newPlateNumber,
            String newSerialNumber,
            String newEngineNumber) {
    }
}
