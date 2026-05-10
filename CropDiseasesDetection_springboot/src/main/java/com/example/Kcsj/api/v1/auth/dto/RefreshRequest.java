package com.example.Kcsj.api.v1.auth.dto;

import javax.validation.constraints.NotBlank;

public class RefreshRequest {
    @NotBlank(message = "refreshToken is required")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

