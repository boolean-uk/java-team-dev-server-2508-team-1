package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("profiles")
public class ProfileController {
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    record PostProfile(
            int user,
            String first_name,
            String last_name,
            String github_username,
            String mobile,
            String bio
    ){}

    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody PostProfile profile) {

        if(profile.first_name == null || profile.first_name == "" || profile.last_name == null || profile.last_name == ""){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findById(profile.user);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>(
                    "User for id "+ profile.user + " not found", HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        Profile newProfile = new Profile(
                user,
                profile.first_name,
                profile.last_name,
                profile.bio,
                "https://github.com/" + profile.github_username,
                profile.mobile
                );

        try {
            return new ResponseEntity<>(profileRepository.save(newProfile), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }
    }

}
