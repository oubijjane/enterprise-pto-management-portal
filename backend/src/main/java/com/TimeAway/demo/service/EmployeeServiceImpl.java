package com.TimeAway.demo.service;

import com.TimeAway.demo.dao.DepartmentRepository;
import com.TimeAway.demo.dao.EmployeeRepository;
import com.TimeAway.demo.dao.RoleDOA;
import com.TimeAway.demo.dao.UserRoleDOA;
import com.TimeAway.demo.dto.DepartmentDTO;
import com.TimeAway.demo.dto.EmployeeDTO;
import com.TimeAway.demo.dto.RoleDTO;
import com.TimeAway.demo.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private PasswordEncoder passwordEncoder;
    private UserRoleDOA userRoleDOA;
    private RoleDOA roleDOA;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRoleDOA userRoleDOA,
                               RoleDOA roleDOA, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRoleDOA = userRoleDOA;
        this.roleDOA = roleDOA;
        this.departmentRepository = departmentRepository;
    }
    @Override
    public Page<EmployeeDTO> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Use the native .map() function on the Page interface to preserve pagination metadata
        return employeeRepository.findAll(pageable).map(employee -> {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(employee.getId());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setPhone(employee.getPhone());
            employeeDTO.setLoginName(employee.getLoginName());

            // Removed password fetching/encoding entirely for security

            employeeDTO.setAccrualRatePerMonth(employee.getAccrualRatePerMonth());
            employeeDTO.setUsedVacationDays(employee.getUsedVacationDays());
            employeeDTO.setThisYearVacationDays(employee.getThisYearVacationDays());
            employeeDTO.setLastYearVacationDays(employee.getLastYearVacationDays());
            employeeDTO.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
            employeeDTO.setNextYearVacationDays(employee.getNextYearVacationDays());
            employeeDTO.setLastAccrualDate(employee.getLastAccrualDate());
            employeeDTO.setLastRolloverYear(employee.getLastRolloverYear());

            // Note: Ensure employeeDTO.getRole() and getCoverageTeam() are initialized
            // to empty lists in the DTO class to avoid NullPointerExceptions here.
            employee.getRoles().forEach(role -> {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId(role.getRole().getId());
                roleDTO.setRoleName(role.getRole().getName());
                employeeDTO.getRole().add(roleDTO);
            });

            employee.getCoveringFor().forEach(coveringFor -> {
                EmployeeDTO.BackupDto backupDto = new EmployeeDTO.BackupDto(
                        coveringFor.getId(),
                        coveringFor.getFirstName(),
                        coveringFor.getLastName()
                );
                employeeDTO.getCoverageTeam().add(backupDto);
            });

            return employeeDTO;
        });
    }

    @Override
    public List<EmployeeDTO> getAllEmployeesByDepartmentId(String loginName) {
        // Use the native .map() function on the Page interface to preserve pagination metadata
        return employeeRepository.findColleaguesByLoginName(loginName).stream().map(employee -> {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(employee.getId());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setPhone(employee.getPhone());
            employeeDTO.setLoginName(employee.getLoginName());

            // Removed password fetching/encoding entirely for security

            employeeDTO.setAccrualRatePerMonth(employee.getAccrualRatePerMonth());
            employeeDTO.setUsedVacationDays(employee.getUsedVacationDays());
            employeeDTO.setThisYearVacationDays(employee.getThisYearVacationDays());
            employeeDTO.setLastYearVacationDays(employee.getLastYearVacationDays());
            employeeDTO.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
            employeeDTO.setNextYearVacationDays(employee.getNextYearVacationDays());
            employeeDTO.setLastAccrualDate(employee.getLastAccrualDate());
            employeeDTO.setLastRolloverYear(employee.getLastRolloverYear());
            DepartmentDTO departmentDTO = new DepartmentDTO();
            departmentDTO.setId(employee.getDepartment().getId());
            departmentDTO.setName(employee.getDepartment().getName());
            employeeDTO.setDepartmentDTO(departmentDTO);
            // Note: Ensure employeeDTO.getRole() and getCoverageTeam() are initialized
            // to empty lists in the DTO class to avoid NullPointerExceptions here.
            employee.getRoles().forEach(role -> {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId(role.getRole().getId());
                roleDTO.setRoleName(role.getRole().getName());
                employeeDTO.getRole().add(roleDTO);
            });

            employee.getCoveringFor().forEach(coveringFor -> {
                EmployeeDTO.BackupDto backupDto = new EmployeeDTO.BackupDto(
                        coveringFor.getId(),
                        coveringFor.getFirstName(),
                        coveringFor.getLastName()
                );
                employeeDTO.getCoverageTeam().add(backupDto);
            });

            return employeeDTO;
        }).toList();
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> employeeDTOS = employees.stream().map(
                employee -> {
                    EmployeeDTO employeeDTO = new EmployeeDTO();
                    employeeDTO.setId(employee.getId());
                    employeeDTO.setFirstName(employee.getFirstName());
                    employeeDTO.setLastName(employee.getLastName());
                    employeeDTO.setEmail(employee.getEmail());
                    employeeDTO.setPhone(employee.getPhone());
                    employeeDTO.setPassword(passwordEncoder.encode(employee.getPassword()));
                    employeeDTO.setAccrualRatePerMonth(employee.getAccrualRatePerMonth());
                    employeeDTO.setUsedVacationDays(employee.getUsedVacationDays());
                    employeeDTO.setThisYearVacationDays(employee.getThisYearVacationDays());
                    employeeDTO.setLastYearVacationDays(employee.getLastYearVacationDays());
                    employeeDTO.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
                    employee.getRoles().forEach(role -> {
                        RoleDTO  roleDTO = new RoleDTO();
                        roleDTO.setId(role.getRole().getId());
                        roleDTO.setRoleName(role.getRole().getName());
                        employeeDTO.getRole().add(roleDTO);
                    });
                    employee.getCoveringFor().forEach(coveringFor -> {
                        EmployeeDTO.BackupDto backupDto =
                                new EmployeeDTO.BackupDto(coveringFor.getId(), coveringFor.getFirstName(), coveringFor.getLastName());
                        employeeDTO.getCoverageTeam().add(backupDto);
                    });
                    employeeDTO.setLoginName(employee.getLoginName());
                    employeeDTO.setNextYearVacationDays(employee.getNextYearVacationDays());
                    employeeDTO.setLastAccrualDate(employee.getLastAccrualDate());
                    employeeDTO.setLastAccrualDate(employee.getLastAccrualDate());
                    employeeDTO.setLastRolloverYear(employee.getLastRolloverYear());
                    employeeDTO.setNextYearVacationDays(employee.getNextYearVacationDays());
                    return  employeeDTO;
                }
        ).toList();
        return employeeDTOS;
    }

    @Override
    public int resetLastYearPTOS() {
        /*Employee employee = employeeRepository.findEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Did not find employee id - " + id));
        employee.setLastYearVacationDays(BigDecimal.ZERO);
        employee.setLastYearUsedVacationDays(BigDecimal.ZERO);*/

        return employeeRepository.bulkResetLastYearPTOS();
    }

    @Override
    public EmployeeDTO getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Did not find employee id - " + id));
        return mapToDTO(employee);
    }

    @Override
    public Employee getEmployeeByIdv2(Integer id) {
        return employeeRepository.findEmployeeById(id).orElseThrow(() -> new RuntimeException("Did not find employee id - " + id));
    }

    @Override
    public Employee findLoginName(String loginName) {
        Employee employee = employeeRepository.findLoginName(loginName);
        if (employee == null) {
            throw new RuntimeException("Employee with loginName " + loginName + " not found");
        }
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setLoginName(employee.getLoginName());
        employeeDTO.setPassword(employee.getPassword());
        employeeDTO.setFirstName(employee.getFirstName());
        employeeDTO.setLastName(employee.getLastName());
        employeeDTO.setEmail(employee.getEmail());

        return employee;
    }

    @Override
    public Page<EmployeeDTO> searchEmployee(String keyWord, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Use the native .map() function on the Page interface to preserve pagination metadata
        return employeeRepository.searchEmployees(keyWord, pageable).map(employee -> {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(employee.getId());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setPhone(employee.getPhone());
            employeeDTO.setLoginName(employee.getLoginName());

            // Removed password fetching/encoding entirely for security

            employeeDTO.setAccrualRatePerMonth(employee.getAccrualRatePerMonth());
            employeeDTO.setUsedVacationDays(employee.getUsedVacationDays());
            employeeDTO.setThisYearVacationDays(employee.getThisYearVacationDays());
            employeeDTO.setLastYearVacationDays(employee.getLastYearVacationDays());
            employeeDTO.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
            employeeDTO.setNextYearVacationDays(employee.getNextYearVacationDays());
            employeeDTO.setLastAccrualDate(employee.getLastAccrualDate());
            employeeDTO.setLastRolloverYear(employee.getLastRolloverYear());

            // Note: Ensure employeeDTO.getRole() and getCoverageTeam() are initialized
            // to empty lists in the DTO class to avoid NullPointerExceptions here.
            employee.getRoles().forEach(role -> {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId(role.getRole().getId());
                roleDTO.setRoleName(role.getRole().getName());
                employeeDTO.getRole().add(roleDTO);
            });

            employee.getCoveringFor().forEach(coveringFor -> {
                EmployeeDTO.BackupDto backupDto = new EmployeeDTO.BackupDto(
                        coveringFor.getId(),
                        coveringFor.getFirstName(),
                        coveringFor.getLastName()
                );
                employeeDTO.getCoverageTeam().add(backupDto);
            });

            return employeeDTO;
        });
    }

    @Override
    public EmployeeDTO findMyProfile(String loginName) {
        Employee employee = employeeRepository.findLoginName(loginName);
        verifyOwner(employee, loginName);
        return mapToDTO(employee);
    }

    private Employee findMyProfiles(String loginName) {
        Employee employee = employeeRepository.findLoginName(loginName);
        verifyOwner(employee, loginName);
        return employee;
    }

    @Override
    public EmployeeDTO updateNextYearVacationDays(EmployeeDTO employee) {
        Employee existingEmployee = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found"));
        existingEmployee.setNextYearVacationDays(employee.getNextYearVacationDays());
        existingEmployee.setLastAccrualDate(employee.getLastAccrualDate());
        Employee savedEmployee = employeeRepository.save(existingEmployee);
        return mapToDTO(savedEmployee);
    }

    @Override
    public Employee addEmployee(EmployeeDTO employee) {

        Employee newEmployee = new Employee();
        newEmployee.setId(null);
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        newEmployee.setLoginName(employee.getLoginName());
        newEmployee.setEmail(employee.getEmail());
        newEmployee.setPhone(employee.getPhone());
        String encodedPassword = passwordEncoder.encode(employee.getPassword());
        newEmployee.setPassword(encodedPassword);
        newEmployee.setHiringDate(employee.getHiringDate());
        if(employee.getDepartmentDTO() != null && employee.getDepartmentDTO().getId() != null) {
            newEmployee.setDepartment(departmentRepository.findById(employee.getDepartmentDTO().getId())
                    .orElseThrow(() -> new RuntimeException("Department with id " + employee.getDepartmentDTO().getId()
                            + " not found")));
        }


        return employeeRepository.save(newEmployee);
    }

    @Override
    public Employee updateDays(Employee employee) {
        Employee existingEmployee = employeeRepository.findEmployeeById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found"));
        existingEmployee.setUsedVacationDays(employee.getUsedVacationDays());
        existingEmployee.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
        existingEmployee.setLastYearVacationDays(employee.getLastYearVacationDays());
        existingEmployee.setThisYearVacationDays(employee.getThisYearVacationDays());
        return employeeRepository.save(existingEmployee);
    }

    @Override
    public Employee addBackup(int id, String loginName) {
        Employee employee = findMyProfiles(loginName);
        EmployeeDTO backUp = getEmployeeById(id);
        Employee existingBackup = employeeRepository.findById(
                backUp.getId()).orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found") );
        employee.getDefaultBackups().add(existingBackup);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(EmployeeDTO employee) {
        // 1. Fetch the existing entity from the database
        Employee existingEmployee = employeeRepository.findEmployeeById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found"));

        // 2. Safely update String fields
        if (employee.getFirstName() != null && !employee.getFirstName().isBlank()) {
            existingEmployee.setFirstName(employee.getFirstName());
        }
        if (employee.getLastName() != null && !employee.getLastName().isBlank()) {
            existingEmployee.setLastName(employee.getLastName());
        }
        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            existingEmployee.setEmail(null);
        } else {
            existingEmployee.setEmail(employee.getEmail());
        }
        if (employee.getPhone() != null && !employee.getPhone().isBlank()) {
            existingEmployee.setPhone(employee.getPhone());
        }
        if (employee.getLoginName() != null && !employee.getLoginName().isBlank()) {
            existingEmployee.setLoginName(employee.getLoginName());
        }
        if (employee.getPassword() != null && !employee.getPassword().isBlank()) {
            existingEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }

        // 3. Safely update BigDecimal fields
        if (employee.getThisYearVacationDays() != null) {
            existingEmployee.setThisYearVacationDays(employee.getThisYearVacationDays());
        }
        if (employee.getLastYearVacationDays() != null) {
            existingEmployee.setLastYearVacationDays(employee.getLastYearVacationDays());
        }
        if (employee.getUsedVacationDays() != null) {
            existingEmployee.setUsedVacationDays(employee.getUsedVacationDays());
        }
        if (employee.getLastYearUsedVacationDays() != null) {
            existingEmployee.setLastYearUsedVacationDays(employee.getLastYearUsedVacationDays());
        }
        if (employee.getAccrualRatePerMonth() != null) {
            existingEmployee.setAccrualRatePerMonth(employee.getAccrualRatePerMonth());
        }
        if (employee.getHiringDate() != null) {
            existingEmployee.setHiringDate(employee.getHiringDate());
        }

        if(employee.getDepartmentDTO() != null && employee.getDepartmentDTO().getId() != null) {
            Department department = departmentRepository.findById(employee.getDepartmentDTO()
                    .getId()).orElseThrow(
                            () -> new RuntimeException("Department with id " + employee.getDepartmentDTO().getId())
            );
            existingEmployee.setDepartment(department);
        }

        // 4. Update Coverage Team
        if (employee.getCoverageTeam() != null) {
            Set<Employee> newBackups = employee.getCoverageTeam().stream()
                    .map(backupDto -> employeeRepository.findEmployeeById(backupDto.id())
                            .orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found")))
                    .collect(Collectors.toSet());
            existingEmployee.setDefaultBackups(newBackups);
        }
        if (employee.getNextYearVacationDays() != null) {
            existingEmployee.setNextYearVacationDays(employee.getNextYearVacationDays());
        }
        userRoleDOA.deleteByEmployeeId(employee.getId());
        assert existingEmployee.getRoles() != null;
        existingEmployee.getRoles().clear();

        if (employee.getRole() != null && !employee.getRole().isEmpty()) {
            employee.getRole().forEach((role) -> {
                UserRole request = new UserRole(); // New instance per loop is safer
                request.setEmployee(existingEmployee);
                Role newRole = roleDOA.findRoleById(role.getId());
                request.setRole(newRole);
                EmplyeeRoleId emplyeeRoleId = new EmplyeeRoleId();
                emplyeeRoleId.setRoleId(role.getId());
                emplyeeRoleId.setUserId(existingEmployee.getId());
                request.setId(emplyeeRoleId);
                userRoleDOA.save(request);
            });
        }
        if(employee.getDepartmentDTO() != null && employee.getDepartmentDTO().getId() != null){
            Department department = departmentRepository.findById(employee.getDepartmentDTO().getId())
                    .orElseThrow(() -> new RuntimeException
                            ("department with id " + employee.getDepartmentDTO().getId() + " not found"));
            existingEmployee.setDepartment(department);
        }

        // 5. Save the updated entity
        Employee savedEmployee = employeeRepository.save(existingEmployee);

        // 6. Map the saved entity back to a DTO to return to the frontend
        return mapToDTO(savedEmployee);
    }
    @Override
    public EmployeeDTO updateYearlyPTOS(EmployeeDTO employee) {
        Employee existingEmployee = employeeRepository.findEmployeeById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee with id " + employee.getId() + " not found"));
        existingEmployee.setLastYearVacationDays(existingEmployee.getThisYearVacationDays());
        existingEmployee.setLastYearUsedVacationDays(existingEmployee.getUsedVacationDays());
        existingEmployee.setThisYearVacationDays(existingEmployee.getNextYearVacationDays());
        existingEmployee.setUsedVacationDays(BigDecimal.ZERO);
        existingEmployee.setNextYearVacationDays(BigDecimal.ZERO);
        existingEmployee.setLastRolloverYear(LocalDate.now(ZoneId.of("Africa/Casablanca")).getYear());
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return mapToDTO(updatedEmployee);
    }

    // --- Helper Method to Map Entity to DTO ---
    private EmployeeDTO mapToDTO(Employee entity) {
        EmployeeDTO dto = new EmployeeDTO();

        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setLoginName(entity.getLoginName());
        dto.setNextYearVacationDays(entity.getNextYearVacationDays());
        dto.setLastRolloverYear(entity.getLastRolloverYear());


        // SECURITY BEST PRACTICE: Deliberately do NOT map the password back to the DTO
        // dto.setPassword(null);

        dto.setThisYearVacationDays(entity.getThisYearVacationDays());
        dto.setLastYearVacationDays(entity.getLastYearVacationDays());
        dto.setUsedVacationDays(entity.getUsedVacationDays());
        dto.setLastYearUsedVacationDays(entity.getLastYearUsedVacationDays());
        dto.setAccrualRatePerMonth(entity.getAccrualRatePerMonth());
        dto.setHiringDate(entity.getHiringDate());

        // Map the Set<Employee> back to List<BackupDto>
        if (entity.getDefaultBackups() != null) {
            List<EmployeeDTO.BackupDto> backupDtos = entity.getDefaultBackups().stream()
                    .map(backup -> new EmployeeDTO.BackupDto(
                            backup.getId(),
                            backup.getFirstName(),
                            backup.getLastName()
                    ))
                    .collect(Collectors.toList());
            dto.setCoverageTeam(backupDtos);
        }


        if (entity.getRoles() != null) {
            List<RoleDTO> roleDtos = entity.getRoles().stream() // 1. Convert HashSet to Stream
                    .map(role -> {                                  // 2. Map each element
                        RoleDTO roleDTO = new RoleDTO();
                        roleDTO.setRoleName(role.getRole().getName());
                        roleDTO.setId(role.getRole().getId());
                        return roleDTO;
                    })
                    .toList();                                      // 3. Collect back into a List (Use .collect(Collectors.toList()) if on Java 15 or older)

            dto.setRole(roleDtos);
            if(entity.getDepartment() != null && entity.getDepartment().getId() != null){
                DepartmentDTO departmentDTO = new DepartmentDTO();
                departmentDTO.setId(entity.getDepartment().getId());
                departmentDTO.setName(entity.getDepartment().getName());
                dto.setDepartmentDTO(departmentDTO);
            }
        }

        return dto;
    }

    @Override
    public void deleteEmployee(Integer id) {
        getEmployeeById(id);
        employeeRepository.deleteById(id);
    }

    private void verifyOwner(Employee employee,String loginName) {
        if(employee.getLoginName() == null || employee.getLoginName().isEmpty()) {
            throw new IllegalStateException("Corrupted record: employee profile not found");
        };
        if(!employee.getLoginName().equals(loginName)) {
            throw new AccessDeniedException("Action denied. You do not have access to this profile.");
        };
    }
}
