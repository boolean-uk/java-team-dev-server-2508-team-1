package com.booleanuk.cohorts.models;


import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"user", "cohort", "role"})
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

  
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties("profile")
    private User user;

    @NotNull(message = "First name is mandatory")
    @NotEmpty(message = "First name cannot be empty")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name can only contain letters and spaces")
    @Column
    private String firstName;

    @NotNull(message = "Last name is mandatory")
    @NotEmpty(message = "Last name cannot be empty")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name can only contain letters and spaces")
    @Column
    private String lastName;

    @Column
    private String username;

    @Column(length = 300)
    private String bio;

    @Column
    private String githubUrl;

    @Column
    private String mobile;

    @Column
    private String specialism;


    @ManyToOne
    @JoinColumn(name = "cohort_id")
    @JsonIgnoreProperties({"profiles", "users", "deliveryLogs", "cohortCourses"})
    private Cohort cohort;

    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties("cohort")
    private Role role;

    @Column(length = 2500000) 
    private String photo;

    public Profile(int id) {
        this.id = id;
    }

    public Profile(User user, String firstName, String lastName, String username, String githubUrl, String mobile,
                   String bio, Role role, String specialism, Cohort cohort, String photo) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.githubUrl = githubUrl;
        this.mobile = mobile;
        this.bio = bio;
        this.role = role;
        this.specialism = specialism;
        this.cohort = cohort;
        this.photo = photo;
    }
}
