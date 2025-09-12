package com.booleanuk.cohorts.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    private int id;
    private int cohortId;
    private String first_name;
    private String last_name;
    private String email;
    private String bio;
    private String githubUrl;
    private String role;

    public Author(int id, int cohortId, String first_name, String last_name, String email, String bio, String githubUrl) {
        this.id = id;
        this.cohortId = cohortId;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.bio = bio;
        this.githubUrl = githubUrl;
    }
}
