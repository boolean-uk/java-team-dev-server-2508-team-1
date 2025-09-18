package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.StudentRequest;
import com.booleanuk.cohorts.payload.request.TeacherEditStudentRequest;
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

    @PatchMapping("{id}")
    public ResponseEntity<?> updateStudent(@PathVariable int id, @RequestBody TeacherEditStudentRequest teacherEditStudentRequest) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Profile profile = profileRepository.findById(user.getProfile().getId()).orElse(null);
        if (profile == null) {
            return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
        }

        if (profile.getRole().getName().name().equals("ROLE_TEACHER")) {
            return new ResponseEntity<>("Teachers can only edit other Students!", HttpStatus.BAD_REQUEST);
        }

        Cohort cohort = cohortRepository.findById(teacherEditStudentRequest.getCohort_id()).orElseThrow();
        Role role = roleRepository.findById(teacherEditStudentRequest.getRole_id()).orElseThrow();

        profile.setCohort(cohort);
        profile.setRole(role);
        profile.setStartDate(teacherEditStudentRequest.getStart_date());
        profile.setEndDate(teacherEditStudentRequest.getEnd_date());


        return new ResponseEntity<>(profileRepository.save(profile),HttpStatus.OK);
    }
  
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

}

