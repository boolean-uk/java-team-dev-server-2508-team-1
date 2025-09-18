package com.booleanuk.cohorts.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {

    private String photo;
    private String first_name;
    private String last_name;
    private String username;
    private String github_username;
    private String email;
    private String mobile;
    private String password;
    private String bio;

    public StudentRequest(){}


}
