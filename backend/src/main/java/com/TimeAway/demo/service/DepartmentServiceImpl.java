package com.TimeAway.demo.service;

import com.TimeAway.demo.dao.DepartmentRepository;
import com.TimeAway.demo.dto.DepartmentDTO;
import com.TimeAway.demo.dto.EmployeeDTO;
import com.TimeAway.demo.entity.Department;
import com.TimeAway.demo.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    final private DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }
    @Override
    public DepartmentDTO getDepartmentById(Long id) {
        return mapToDTO(departmentRepository.findById(id).orElseThrow(()->
                new RuntimeException("Department with id " + id + " not found!")));
    }

    @Override
    public DepartmentDTO addDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setName(departmentDTO.getName());
        Department savedDep = departmentRepository.save(department);

        return mapToDTO(savedDep);
    }

    @Override
    public List<DepartmentDTO> getAllDepartments() {

        return departmentRepository.findAll()
                .stream().map((department) -> mapToDTO(department)).toList();
    }

    @Override
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        return null;
    }

    @Override
    public void deleteDepartment(Long id) {

    }

    private DepartmentDTO mapToDTO(Department department) {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setId(department.getId());
        departmentDTO.setName(department.getName());
        EmployeeDTO employeeDTO = new EmployeeDTO();
        if(department.getResponsible() != null) {
        employeeDTO.setFirstName(department.getResponsible().getFirstName());
        employeeDTO.setLastName(department.getResponsible().getLastName());
        employeeDTO.setId(department.getResponsible().getId());
        departmentDTO.setResponsible(employeeDTO);
        }
        return departmentDTO;
    }
}
