package com.jpm.erp.domains.core.dto;

import java.util.List;
import java.util.UUID;

public record UserDTO(UUID id, String username, String email, String fullName, String role, List<String> permissions) {
}
