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

    public String getPhoto() { return photo; }
    public String getFirst_name() { return first_name; }
    public String getLast_name() { return last_name; }
    public String getUsername() { return username; }
    public String getGithub_username() { return github_username; }

    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPassword() { return password; }
    public String getBio() { return bio; }



}
