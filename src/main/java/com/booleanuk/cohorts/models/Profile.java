package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("users")
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
    private String bio;

    @Column
    private String githubUrl;

    @Column
    private String mobile;

    public Profile(int id) {
        this.id = id;
    }

    public Profile(User user, String firstName, String lastName, String bio, String githubUrl, String mobile) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.mobile = mobile;
    }
}
