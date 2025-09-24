package com.booleanuk.cohorts.payload.request;

import com.booleanuk.cohorts.models.Cohort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private Cohort cohort;

    public SignupRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
