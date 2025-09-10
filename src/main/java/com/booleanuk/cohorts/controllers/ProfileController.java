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

import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            String bio,
            String specialty,
            String start_date,
            String end_date
    ){}

    record TargetUser(
            int user_id
    ){}

    @PatchMapping("{id}")
    public ResponseEntity<?> updateUserWithProfile(@PathVariable int id, @RequestBody TargetUser targetUser) {
        Profile profile = profileRepository.findById(id).orElse(null);
        if (profile == null){
            return new ResponseEntity<>("Could not add profile to user because the profile does not exist", HttpStatus.NOT_FOUND);
        }
        User user = userRepository.findById(targetUser.user_id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        user.setProfile(profile.);
        return new ResponseEntity<>("Profile added to user with email: " + user.getEmail(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody PostProfile profile) {

        if(profile.first_name == null || profile.first_name == "" || profile.last_name == null || profile.last_name == ""){
            return new ResponseEntity<>("First and last name can't be empty or NULL. First name: " + profile.first_name + " Last name: " + profile.last_name, HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findById(profile.user);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("User for id "+ profile.user + " not found", HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        Profile newProfile = null;
        try {
            newProfile = new Profile(
                    user,
                    profile.first_name,
                    profile.last_name,
                    profile.bio,
                    "https://github.com/" + profile.github_username,
                    profile.mobile,
                    profile.specialty,
                    LocalDate.parse(profile.start_date),
                    LocalDate.parse(profile.end_date)
                    );
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Wrong formatting for start_date or end_date. Plese use the following format: 2025-09-14",
                    HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(profileRepository.save(newProfile), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }
    }

}
