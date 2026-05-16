package com.jpmonitor.domains.hr.service;

import com.jpmonitor.domains.hr.dto.EmployeeDTO;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    List<EmployeeDTO> getAllEmployees(String department, String status);

    EmployeeDTO getEmployee(UUID id);

    EmployeeDTO createEmployee(EmployeeDTO dto);

    EmployeeDTO updateEmployee(UUID id, EmployeeDTO dto);

    void deleteEmployee(UUID id);
}