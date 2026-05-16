package com.jpmonitor.domains.hr.service.impl;

import com.jpmonitor.domains.core.repository.LocationRepository;
import com.jpmonitor.domains.hr.dto.EmployeeDTO;
import com.jpmonitor.domains.hr.entity.Employee;
import com.jpmonitor.domains.hr.repository.EmployeeRepository;
import com.jpmonitor.domains.hr.service.EmployeeService;
import com.jpmonitor.platform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees(String department, String status) {
        return employeeRepository.findAll().stream()
                .filter(e -> department == null || department.equals(e.getDepartment()))
                .filter(e -> status == null || status.equals(e.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return mapToDTO(employee);
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        updateEmployeeFromDTO(employee, dto);
        Employee saved = employeeRepository.save(employee);
        return mapToDTO(saved);
    }

    @Override
    public EmployeeDTO updateEmployee(UUID id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        updateEmployeeFromDTO(employee, dto);
        Employee saved = employeeRepository.save(employee);
        return mapToDTO(saved);
    }

    @Override
    public void deleteEmployee(UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        employeeRepository.deleteById(id);
    }

    // ==================== HELPER METHODS ====================

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