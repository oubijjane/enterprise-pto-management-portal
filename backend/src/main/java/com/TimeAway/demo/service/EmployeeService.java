package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.EmployeeDTO;
import com.TimeAway.demo.entity.Employee;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService {
    Page<EmployeeDTO> getAllEmployees(int  page, int size);
    List<EmployeeDTO> getAllEmployeesByDepartmentId(String loginName);
    List<EmployeeDTO> getAllEmployees();
    int resetLastYearPTOS();
    EmployeeDTO getEmployeeById(Integer id);
    Employee getEmployeeByIdv2(Integer id);
    Employee findLoginName(String loginName);
    Page<EmployeeDTO> searchEmployee(String firstName, int  page, int size);
    EmployeeDTO findMyProfile(String loginName);
    EmployeeDTO updateNextYearVacationDays(EmployeeDTO employee);
    Employee addEmployee(EmployeeDTO employee);
    Employee updateDays(Employee employee);
    Employee addBackup(int id, String loginName);
    EmployeeDTO updateEmployee(EmployeeDTO employee);
    EmployeeDTO updateYearlyPTOS(EmployeeDTO employee);
    void deleteEmployee(Integer id);
}
