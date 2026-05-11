package com.jpmonitor.api.controller;

import com.jpmonitor.domains.core.dto.LocationDTO;
import com.jpmonitor.domains.core.entity.Location;
import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;

    @GetMapping
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
        return ResponseEntity.ok(mapToDTO(location));
    }

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO dto) {
        Location location = new Location();
        updateLocationFromDTO(location, dto);

        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable UUID id, @Valid @RequestBody LocationDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));

        updateLocationFromDTO(location, dto);
        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location", id);
        }
        locationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods
    private void updateLocationFromDTO(Location location, LocationDTO dto) {
        location.setCode(dto.code());
        location.setName(dto.name());
        location.setAddress(dto.address());
        location.setType(dto.type());
        // Project linking logic skipped for brevity - would need ProjectRepository
    }

    private LocationDTO mapToDTO(Location loc) {
        return new LocationDTO(
                loc.getId(),
                loc.getProject() != null ? loc.getProject().getId() : null,
                loc.getCode(),
                loc.getName(),
                loc.getAddress(),
                loc.getType());
    }
}
