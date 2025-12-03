package com.jpm.erp.domains.fleet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EquipmentDTO(
                UUID id,
                String code,
                String name,
                String model,
                String type,
                String brand,
                String serialNumber,
                String engineNumber,
                String chassisNumber,
                String plateNumber,
                Integer manufactureYear,
                String status,
                UUID locationId,
                String location,
                BigDecimal hourMeter,
                BigDecimal kilometer,
                String owner) {
}
