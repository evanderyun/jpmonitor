package com.jpmonitor.domains.hr.dto;

import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        String name,
        String position,
        String department,
        String role, // e.g. Mechanic
        String status,
        String joinedDate,
        UUID locationId // Added for location assignment
) {
}
