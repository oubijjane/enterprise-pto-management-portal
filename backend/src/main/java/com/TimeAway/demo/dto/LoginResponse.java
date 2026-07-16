package com.TimeAway.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private String token;

    private long expiresIn;

    private String username;

    private List<String> roles;

}

