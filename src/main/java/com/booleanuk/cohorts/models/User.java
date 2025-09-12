package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    // The AuthController uses a built-in class for Users that expects a Username, we don't use it elsewhere in the code.
    @Transient
    @Size(min = 7, max = 50, message = "Username must be between 7 and 50 characters")
    private String username = this.email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "cohort_id", nullable = true)
    @JsonIgnoreProperties({"users","cohort_courses"})
    private Cohort cohort;


    @OneToOne
    @JoinColumn(name = "profile_id")
    @JsonIgnoreProperties("users")
    private Profile profile;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, Cohort cohort) {
        this.email = email;
        this.password = password;
        this.cohort = cohort;
    }
    public User(String email, String password, Cohort cohort, Profile profile) {
        this.email = email;
        this.password = password;
        this.cohort = cohort;
        this.profile = profile;
    }
}
