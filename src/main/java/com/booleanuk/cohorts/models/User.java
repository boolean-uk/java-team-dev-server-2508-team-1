package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
*/

@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"profile", "posts", "comments", "likedPosts", "cohort"})
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
    @JsonIncludeProperties({"id", "cohort_courses"})
    private Cohort cohort;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIncludeProperties({"id", "content", "likes", "timeCreated", "timeUpdated" })
    private List<Post> posts;

    @ManyToMany
    @JoinTable(name = "user_liked_posts", 
               joinColumns = @JoinColumn(name = "user_id"), 
               inverseJoinColumns = @JoinColumn(name = "post_id"))
    @JsonIncludeProperties(value="id")
    private Set<Post> likedPosts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIncludeProperties({"id","body" })
    private List<Comment> comments;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user", "role", "cohort"})
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
