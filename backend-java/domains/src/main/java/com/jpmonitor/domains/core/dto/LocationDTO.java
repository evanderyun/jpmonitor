package com.jpmonitor.domains.core.dto;

import java.util.UUID;

public record LocationDTO(
    UUID id,
    UUID projectId,
    String code,
    String name,
    String address,
    String type
) {}
