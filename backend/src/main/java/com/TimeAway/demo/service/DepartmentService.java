package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {
    DepartmentDTO getDepartmentById(Long id);
    DepartmentDTO addDepartment(DepartmentDTO departmentDTO);
    List<DepartmentDTO> getAllDepartments();
    DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
}
