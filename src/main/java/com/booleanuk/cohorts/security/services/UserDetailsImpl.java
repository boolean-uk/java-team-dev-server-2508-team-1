package com.booleanuk.cohorts.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.booleanuk.cohorts.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class UserDetailsImpl  implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(int id, String username, String email, String password, String firstName, String lastName, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = email;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        
        // Get firstName and lastName from profile, with fallbacks if profile is null
        String firstName = "";
        String lastName = "";
        
        if (user.getProfile() != null) {
            firstName = user.getProfile().getFirstName() != null ? user.getProfile().getFirstName() : "";
            lastName = user.getProfile().getLastName() != null ? user.getProfile().getLastName() : "";
            System.out.println("Profile found for user " + user.getEmail() + ": " + firstName + " " + lastName);
        } else {
            System.out.println("No profile found for user " + user.getEmail());
        }
        
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                firstName,
                lastName,
                authorities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
