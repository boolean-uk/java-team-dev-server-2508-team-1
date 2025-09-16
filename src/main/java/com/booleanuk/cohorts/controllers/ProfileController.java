package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.RoleRepository;
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
import java.util.List;
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
    private CohortRepository cohortRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    record PostProfile(
            int userId,
            String first_name,
            String last_name,
            String username,
            String mobile,
            String github_username,
            String bio,
            String role,
            String specialism,
            int cohort,
            String start_date,
            String end_date,
            String photo
    ){}

    @GetMapping
    public ResponseEntity<ProfileListResponse> getAllProfiles() {
        ProfileListResponse profileListResponse = new ProfileListResponse();
        profileListResponse.set(this.profileRepository.findAll());
        return ResponseEntity.ok(profileListResponse);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable int id){
        ProfileResponse profileResponse = new ProfileResponse();

        Profile profile = this.profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }
        profileResponse.set(profile);
        return ResponseEntity.ok(profileResponse);
    }

    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody PostProfile profile) {

        if(profile.first_name == null || profile.first_name == "" || profile.last_name == null || profile.last_name == ""){
            return new ResponseEntity<>("First and last name can't be empty or NULL. First name: " + profile.first_name + " Last name: " + profile.last_name, HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findById(profile.userId);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("User for id "+ profile.userId + " not found", HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        Optional<Role> optionalRole = roleRepository.findByName(ERole.valueOf(profile.role));
        if (optionalRole.isEmpty()) {
            return new ResponseEntity<>("Role for id "+ profile.role + " not found", HttpStatus.BAD_REQUEST);
        }

        Role role = optionalRole.get();

        Optional<Cohort> optionalCohort = cohortRepository.findById(profile.cohort);
        if (optionalCohort.isEmpty()) {
            return new ResponseEntity<>("Cohort for id "+ profile.cohort + " not found", HttpStatus.BAD_REQUEST);
        }

        Cohort cohort = optionalCohort.get();

        Profile newProfile = null;
        try {
            newProfile = new Profile(
                    user,
                    profile.first_name,
                    profile.last_name,
                    profile.username,
                    "https://github.com/" + profile.github_username,
                    profile.mobile,
                    profile.bio,
                    role,
                    profile.specialism,
                    cohort,
                    LocalDate.parse(profile.start_date),
                    LocalDate.parse(profile.end_date),
                    profile.photo
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
