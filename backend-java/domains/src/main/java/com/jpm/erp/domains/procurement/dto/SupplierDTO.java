package com.jpm.erp.domains.procurement.dto;

import java.util.UUID;

public record SupplierDTO(
    UUID id,
    String name,
    String type, // Parts Vendor, Service Workshop
    String contactPerson,
    String phone,
    String address,
    Integer rating
) {}
