package com.TimeAway.demo.restController;

import com.TimeAway.demo.dto.AuthRequest;
import com.TimeAway.demo.dto.LoginResponse;
import com.TimeAway.demo.entity.Employee;
import com.TimeAway.demo.service.AuthenticationService;
import com.TimeAway.demo.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody AuthRequest loginUserDto) {
        Employee authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        List<String> roles = authenticatedUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        loginResponse.setRoles(roles);
        loginResponse.setUsername(authenticatedUser.getUsername());
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/login")
    public ResponseEntity<String> authenticate5() {


        return new ResponseEntity<>("hey", HttpStatus.OK);
    }
}
