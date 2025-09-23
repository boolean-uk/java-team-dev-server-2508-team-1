package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.StudentRequest;
import com.booleanuk.cohorts.payload.request.TeacherEditStudentRequest;
import com.booleanuk.cohorts.payload.request.TeacherRequest;
import com.booleanuk.cohorts.payload.response.ProfileListResponse;
import com.booleanuk.cohorts.payload.response.Response;
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

import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("teachers")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired private CohortRepository cohortRepository;
    @Autowired private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;
  
    @GetMapping
    public ResponseEntity<?> getAllTeachers(){
        List<Profile> allProfiles = this.profileRepository.findAll();

        List<Profile> teachers = new ArrayList<>();

        for (Profile profile : allProfiles) {
            if (profile.getRole() != null &&
                    profile.getRole().getName() != null &&
                    "ROLE_TEACHER".equals(profile.getRole().getName().name())) {
                teachers.add(profile);
            }
        }

        ProfileListResponse teacherListResponse = new ProfileListResponse();
        teacherListResponse.set(teachers);

        return ResponseEntity.ok(teacherListResponse);
    }
  
    @GetMapping("{id}")
    public ResponseEntity<?> getTeachersByCohortId(@PathVariable int id){
        Cohort cohort = cohortRepository.findById(id).orElse(null);
        if (cohort == null){
            return new ResponseEntity<>("Cohort not found", HttpStatus.NOT_FOUND);
        }

        List<Profile> allProfiles = profileRepository.findAll();

        List<Profile> teachers = new ArrayList<>();

        for (Profile profile : allProfiles) {
            if (profile.getRole() != null &&
                    profile.getRole().getName() != null &&
                    "ROLE_TEACHER".equals(profile.getRole().getName().name()) &&
                    profile.getCohort().getId() == cohort.getId()) {
                teachers.add(profile);
            }
        }
        ProfileListResponse teacherListResponse = new ProfileListResponse();
        teacherListResponse.set(teachers);

        return ResponseEntity.ok(teacherListResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable int id, @RequestBody TeacherRequest teacherRequest) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Profile profile = profileRepository.findById(user.getProfile().getId()).orElse(null);
        if (profile == null) {
            return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
        }

        if (profile.getRole().getName().name().equals("ROLE_STUDENT")) {
            return new ResponseEntity<>("Only users with the TEACHER role can be viewed.", HttpStatus.BAD_REQUEST);
        }

        if (teacherRequest.getPhoto() != null) {
            profile.setPhoto(teacherRequest.getPhoto());
        }

        if (teacherRequest.getFirst_name() != null) {
            profile.setFirstName(teacherRequest.getFirst_name());
        }

        if (teacherRequest.getLast_name() != null) {
            profile.setLastName(teacherRequest.getLast_name());
        }

        if (teacherRequest.getUsername() != null) {
            profile.setUsername(teacherRequest.getUsername());
        }

        if (teacherRequest.getGithub_username() != null) {
            profile.setGithubUrl(teacherRequest.getGithub_username());
        }

        if (teacherRequest.getEmail() != null) {
            boolean emailExists = userRepository.existsByEmail(teacherRequest.getEmail());
            if (emailExists && !teacherRequest.getEmail().equals(user.getEmail())){
                return new ResponseEntity<>("Email is already in use", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(teacherRequest.getEmail());
        }

        if (teacherRequest.getMobile() != null) {
            profile.setMobile(teacherRequest.getMobile());
        }

        if (teacherRequest.getPassword() != null && !teacherRequest.getPassword().isBlank()) {
            user.setPassword(encoder.encode(teacherRequest.getPassword()));
        }

        if (teacherRequest.getBio() != null) {
            profile.setBio(teacherRequest.getBio());
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

