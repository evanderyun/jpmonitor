package com.jpm.erp.api.controller;
import com.jpm.erp.domains.core.dto.LocationDTO;
import com.jpm.erp.domains.core.entity.Location;
import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public List<LocationDTO> getAllLocations() { return locationRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable UUID id) { Location loc = locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", id)); return ResponseEntity.ok(mapToDTO(loc)); }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO dto) { Location loc = new Location(); updateLocationFromDTO(loc, dto); return ResponseEntity.ok(mapToDTO(locationRepository.save(loc))); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable UUID id, @Valid @RequestBody LocationDTO dto) { Location loc = locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", id)); updateLocationFromDTO(loc, dto); return ResponseEntity.ok(mapToDTO(locationRepository.save(loc))); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) { if (!locationRepository.existsById(id)) throw new ResourceNotFoundException("Location", id); locationRepository.deleteById(id); return ResponseEntity.ok().build(); }

    private void updateLocationFromDTO(Location loc, LocationDTO dto) { loc.setCode(dto.code()); loc.setName(dto.name()); loc.setAddress(dto.address()); loc.setType(dto.type()); }
    private LocationDTO mapToDTO(Location l) { return new LocationDTO(l.getId(), l.getProject() != null ? l.getProject().getId() : null, l.getCode(), l.getName(), l.getAddress(), l.getType()); }
}
