package com.jpm.erp.api.controller;

import com.jpm.erp.domains.hr.dto.EmployeeDTO;
import com.jpm.erp.domains.hr.entity.Employee;
import com.jpm.erp.domains.hr.repository.EmployeeRepository;
import com.jpm.erp.domains.core.repository.LocationRepository;
import com.jpm.erp.platform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    public List<EmployeeDTO> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {

        List<Employee> employees = employeeRepository.findAll();

        // Apply filters
        return employees.stream()
                .filter(e -> department == null || department.equals(e.getDepartment()))
                .filter(e -> status == null || status.equals(e.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return ResponseEntity.ok(mapToDTO(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        Employee employee = new Employee();
        updateEmployeeFromDTO(employee, dto);

        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        updateEmployeeFromDTO(employee, dto);
        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods
    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO dto) {
        employee.setName(dto.name());
        employee.setPosition(dto.position());
        employee.setDepartment(dto.department());
        employee.setStatus(dto.status() != null ? dto.status() : "Active");

        if (dto.joinedDate() != null) {
            employee.setJoinedDate(LocalDate.parse(dto.joinedDate()));
        }

        // Handle locationId if provided
        if (dto.locationId() != null) {
            employee.setLocation(locationRepository.findById(dto.locationId())
                    .orElse(null));
        }
    }

    private EmployeeDTO mapToDTO(Employee e) {
        return new EmployeeDTO(
                e.getId(),
                e.getName(),
                e.getPosition(),
                e.getDepartment(),
                e.getPosition(), // Role simplified as position
                e.getStatus(),
                e.getJoinedDate() != null ? e.getJoinedDate().toString() : null,
                e.getLocation() != null ? e.getLocation().getId() : null);
    }
}
