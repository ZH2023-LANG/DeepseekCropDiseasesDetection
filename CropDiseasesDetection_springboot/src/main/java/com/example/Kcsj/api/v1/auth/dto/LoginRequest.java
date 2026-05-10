package com.example.Kcsj.api.v1.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "username is required")
    @Size(min = 1, max = 64, message = "username length must be <=64")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 1, max = 128, message = "password length must be <=128")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

