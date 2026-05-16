package com.jpmonitor.api.mcp;

import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base ERP MCP Tools — foundational lookups for users and locations.
 */
@Component
@RequiredArgsConstructor
public class ErpMcpTools {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    /**
     * Cari data user berdasarkan username atau email.
     */
    @Tool(description = "Mencari data user berdasarkan username atau email")
    public List<Map<String, Object>> cari_user(
            @ToolParam(description = "Username atau email yang dicari") String keyword
    ) {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase())
                        || u.getEmail().toLowerCase().contains(keyword.toLowerCase())
                        || u.getFullName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::toUserMap)
                .collect(Collectors.toList());
    }

    /**
     * Cari lokasi proyek berdasarkan kode atau nama.
     */
    @Tool(description = "Mencari lokasi proyek berdasarkan kode atau nama lokasi")
    public List<Map<String, Object>> lokasi_project(
            @ToolParam(description = "Kode atau nama lokasi yang dicari") String keyword
    ) {
        return locationRepository.findAll().stream()
                .filter(l -> l.getCode().toLowerCase().contains(keyword.toLowerCase())
                        || l.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::toLocationMap)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private Map<String, Object> toUserMap(User u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "fullName", u.getFullName(),
                "isActive", u.getIsActive(),
                "role", u.getRole() != null ? u.getRole().getName() : null
        );
    }

    private Map<String, Object> toLocationMap(Location l) {
        return Map.of(
                "id", l.getId(),
                "code", l.getCode(),
                "name", l.getName(),
                "type", l.getType(),
                "address", l.getAddress(),
                "projectId", l.getProjectId()
        );
    }
}
