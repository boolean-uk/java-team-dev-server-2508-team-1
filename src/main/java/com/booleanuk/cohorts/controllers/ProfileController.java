package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("profiles")
public class ProfileController {
    @Autowired
    private ProfileRepository profileRepository;

    record PostProfile(
            int user,
            String first_name,
            String last_name,
            String github_username,
            String mobile,
            String bio
    ){}

    @PostMapping
    public ResponseEntity<Profile> createProfile(@RequestBody PostProfile profile) {
        User user = profileRepository.findUserById(profile.user);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Profile newProfile = new Profile(
                user,
                profile.first_name,
                profile.last_name,
                "https://github.com/" + profile.github_username,
                profile.mobile,
                profile.bio
                );

        return new ResponseEntity<>(profileRepository.save(newProfile), HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
