package com.jpm.erp.domains.logistics.dto;

import java.util.List;
import java.util.UUID;

/**
 * Java 21 Record: Immutable DTO for Goods Shipment with items
 */
public record GoodsShipmentDTO(
                UUID id,
                String doNumber,
                String date,
                UUID sourceLocationId,
                String sourceLocationName,
                String targetType,
                UUID targetLocationId,
                String targetLocationName,
                UUID targetSupplierId,
                String targetSupplierName,
                String transportProvider,
                UUID driverEmployeeId,
                String driverEmployeeName,
                UUID vehicleEquipmentId,
                String vehicleEquipmentCode,
                String externalDriverName,
                String externalVehicleDesc,
                String policeNumber,
                String status,
                String notes,
                List<ShipmentItemDTO> items // Added for stock reduction
) {
}
