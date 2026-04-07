package com.jpm.erp.api.controller;
import com.jpm.erp.domains.core.dto.EmployeeDTO;
import com.jpm.erp.domains.core.entity.Employee;
import com.jpm.erp.domains.core.repository.EmployeeRepository;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public List<EmployeeDTO> getAllEmployees(@RequestParam(required = false) String department, @RequestParam(required = false) String status) {
        return employeeRepository.findAll().stream()
                .filter(e -> department == null || department.equals(e.getDepartment()))
                .filter(e -> status == null || status.equals(e.getDepartment()))
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable UUID id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return ResponseEntity.ok(mapToDTO(emp));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        Employee emp = new Employee();
        updateFromDTO(emp, dto);
        return ResponseEntity.ok(mapToDTO(employeeRepository.save(emp)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        updateFromDTO(emp, dto);
        return ResponseEntity.ok(mapToDTO(employeeRepository.save(emp)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        if (!employeeRepository.existsById(id)) throw new ResourceNotFoundException("Employee", id);
        employeeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void updateFromDTO(Employee emp, EmployeeDTO dto) {
        emp.setFullName(dto.fullName());
        emp.setPosition(dto.position());
        emp.setDepartment(dto.department());
        emp.setPhone(dto.phone());
        emp.setEmail(dto.email());
        emp.setJoinDate(dto.joinDate() != null ? java.time.LocalDate.parse(dto.joinDate()) : null);
    }

    private EmployeeDTO mapToDTO(Employee e) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getPosition(), e.getDepartment(), e.getPhone(), e.getEmail(), e.getJoinDate() != null ? e.getJoinDate().toString() : null, null);
    }
}
