package com.booleanuk.cohorts.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResponse {
    private String token;
    private String message;

    public RefreshTokenResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}