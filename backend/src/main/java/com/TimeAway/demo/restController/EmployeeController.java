package com.TimeAway.demo.restController;

import com.TimeAway.demo.dto.EmployeeDTO;
import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeDTO> me(@AuthenticationPrincipal UserDetails user) {
        return new ResponseEntity<>(employeeService.findMyProfile(user.getUsername()), HttpStatus.OK);
    }



    @GetMapping("/all")
    public ResponseEntity<Page<EmployeeDTO>> getAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(employeeService.getAllEmployees(page, size), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDTO>> searchEmployees(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String keyword) {
        return new ResponseEntity<>(employeeService.searchEmployee(keyword, page, size), HttpStatus.OK);
    }

    @GetMapping("/department")
    public ResponseEntity<List<EmployeeDTO>> getEmployeeByDepartment(
                                                             @AuthenticationPrincipal UserDetails user) {
        return new ResponseEntity<>(employeeService.getAllEmployeesByDepartmentId(user.getUsername()), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Employee> addNewEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return new ResponseEntity<>(employeeService.addEmployee(employeeDTO), HttpStatus.OK);
    }

    @PutMapping("/backup/{id}")
    public ResponseEntity<Employee> addBackUps(@PathVariable int id, Principal principal) {
        return new ResponseEntity<>(employeeService.addBackup(id, principal.getName()), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<EmployeeDTO> updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return new ResponseEntity<>(employeeService.updateEmployee(employeeDTO), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable int id) {
        return new ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return new ResponseEntity<>("the employee with the id: " + id + " has been deleted",HttpStatus.NO_CONTENT);
    }
}
