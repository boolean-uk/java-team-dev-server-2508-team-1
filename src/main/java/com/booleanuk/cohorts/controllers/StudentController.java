
package com.booleanuk.cohorts.controllers;


import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.StudentRequest;
import com.booleanuk.cohorts.payload.response.ProfileListResponse;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("students")
public class StudentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

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

        if (studentRequest.getPassword() != null) {
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
