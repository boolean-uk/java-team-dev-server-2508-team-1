
package com.booleanuk.cohorts.controllers;


import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.StudentRequest;
import com.booleanuk.cohorts.payload.response.MessageResponse;
import com.booleanuk.cohorts.payload.response.ProfileListResponse;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.RoleRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("students")
public class StudentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CohortRepository cohortRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping
    public ResponseEntity<ProfileListResponse> getAllStudents() {
        List<Profile> allProfiles = this.profileRepository.findAll();

        List<Profile> students = new ArrayList<>();

        for (Profile profile : allProfiles) {
            if (profile.getRole() != null &&
                    profile.getRole().getName() != null &&
                    "ROLE_STUDENT".equals(profile.getRole().getName().name())) {
                students.add(profile);
            }
        }

        ProfileListResponse studentListResponse = new ProfileListResponse();
        studentListResponse.set(students);

        return ResponseEntity.ok(studentListResponse);
    }

    record PostUserProfile(
            String first_name,
            String last_name,
            String username,
            String github_username,
            String email,
            String mobile,
            String password,
            String bio,
            String role,
            String specialism,
            int cohort,
            String start_date,
            String end_date,
            String photo
    ){}

    @PostMapping("/create")
    public  ResponseEntity<?> createNewStudent(@RequestBody PostUserProfile newUserProfile){

        System.err.println(newUserProfile);
        System.out.println("test----------------------------");

        // Lag ny bruker
        if (userRepository.existsByEmail(newUserProfile.email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }


        String emailRegex = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";
        String passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[#?!@$%^&-]).{8,}$";

        if(!newUserProfile.email.matches(emailRegex))
            return ResponseEntity.badRequest().body(new MessageResponse("Email is incorrectly formatted"));

        if(!newUserProfile.password.matches(passwordRegex))
            return  ResponseEntity.badRequest().body(new MessageResponse("Password is incorrectly formatted"));

        // Create a new user add salt here if using one
        User user = new User(newUserProfile.email, encoder.encode(newUserProfile.password));

        //Lag ny profil som er koblet opp til ny bruker

        if(newUserProfile.first_name == null || newUserProfile.first_name == "" || newUserProfile.last_name == null || newUserProfile.last_name == ""){
            return new ResponseEntity<>("First and last name can't be empty or NULL. First name: " + newUserProfile.first_name + " Last name: " + newUserProfile.last_name, HttpStatus.BAD_REQUEST);
        }


        Optional<Role> optionalRole = roleRepository.findByName(ERole.valueOf(newUserProfile.role));
        if (optionalRole.isEmpty()) {
            return new ResponseEntity<>("Role for id "+ newUserProfile.role + " not found", HttpStatus.BAD_REQUEST);
        }

        Role role = optionalRole.get();

        Optional<Cohort> optionalCohort = cohortRepository.findById(newUserProfile.cohort);
        if (optionalCohort.isEmpty()) {
            return new ResponseEntity<>("Cohort for id "+ newUserProfile.cohort + " not found", HttpStatus.BAD_REQUEST);
        }

        Cohort cohort = optionalCohort.get();

        Profile newProfile = null;
        try {
            newProfile = new Profile(
                    user,
                    newUserProfile.first_name,
                    newUserProfile.last_name,
                    newUserProfile.username,
                    "https://github.com/" + newUserProfile.github_username,
                    newUserProfile.mobile,
                    newUserProfile.bio,
                    role,
                    newUserProfile.specialism,
                    cohort,
                    newUserProfile.photo
            );
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Wrong formatting for start_date or end_date. Plese use the following format: 2025-09-14",
                    HttpStatus.BAD_REQUEST);
        }

        newProfile.setUser(user);
        user.setProfile(newProfile);


        try {
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }

    }

    @PatchMapping("{id}")
    public ResponseEntity<?> updateStudent(@PathVariable int id, @RequestBody StudentRequest studentRequest) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Profile profile = profileRepository.findById(user.getProfile().getId()).orElse(null);
        if (profile == null) {
            return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
        }

        if (profile.getRole().getName().name().equals("ROLE_TEACHER")) {
            return new ResponseEntity<>("Only users with the STUDENT role can be viewed.", HttpStatus.BAD_REQUEST);
        }

        if (studentRequest.getPhoto() != null) {
            profile.setPhoto(studentRequest.getPhoto());
        }

        if (studentRequest.getFirst_name() != null) {
            profile.setFirstName(studentRequest.getFirst_name());
        }

        if (studentRequest.getLast_name() != null) {
            profile.setLastName(studentRequest.getLast_name());
        }

        if (studentRequest.getUsername() != null) {
            profile.setUsername(studentRequest.getUsername());
        }

        if (studentRequest.getGithub_username() != null) {
            profile.setGithubUrl(studentRequest.getGithub_username());
        }


        if (studentRequest.getEmail() != null) {
            boolean emailExists = userRepository.existsByEmail(studentRequest.getEmail());
            if (emailExists && !studentRequest.getEmail().equals(user.getEmail())){
                return new ResponseEntity<>("Email is already in use", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(studentRequest.getEmail());
        }

        if (studentRequest.getMobile() != null) {
            profile.setMobile(studentRequest.getMobile());
        }

        if (studentRequest.getPassword() != null && !studentRequest.getPassword().isBlank()) {
            user.setPassword(encoder.encode(studentRequest.getPassword()));
        }

        if (studentRequest.getBio() != null) {
            profile.setBio(studentRequest.getBio());
        }

        profileRepository.save(profile);

        user.setProfile(profile);

        try {
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }
    }

}
