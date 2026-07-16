package com.TimeAway.demo.service;

import com.TimeAway.demo.dao.EmployeeRepository;
import com.TimeAway.demo.dto.AuthRequest;
import com.TimeAway.demo.entity.Employee;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final EmployeeRepository employeeRepository;

    private final AuthenticationManager authenticationManager;


    public AuthenticationService(EmployeeRepository employeeRepository, AuthenticationManager authenticationManager) {
        this.employeeRepository = employeeRepository;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public Employee authenticate(AuthRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        return employeeRepository.findLoginName(input.getUsername());
    }
}

