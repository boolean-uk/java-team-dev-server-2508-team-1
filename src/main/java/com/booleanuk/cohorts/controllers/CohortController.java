package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.ProfileRequest;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("cohorts")
public class CohortController {
    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @GetMapping
    public ResponseEntity<CohortListResponse> getAllCohorts() {
        CohortListResponse cohortListResponse = new CohortListResponse();
        cohortListResponse.set(this.cohortRepository.findAll());
        return ResponseEntity.ok(cohortListResponse);
    }

    @GetMapping("{id}")
    public ResponseEntity<Response> getCohortById(@PathVariable int id) {
        Cohort cohort = this.cohortRepository.findById(id).orElse(null);
        if (cohort == null || cohort.getProfiles().isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        CohortResponse cohortResponse = new CohortResponse();
        cohortResponse.set(cohort);
        return ResponseEntity.ok(cohortResponse);
    }

    @GetMapping("/teacher/{id}")
    public ResponseEntity<?> getCohorstByUserId(@PathVariable int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return new ResponseEntity<>("User for id " + Integer.valueOf(id) + " not found", HttpStatus.NOT_FOUND);
        Profile teacherProfile = profileRepository.findById(user.getProfile().getId()).orElse(null);
        if (teacherProfile == null) return new ResponseEntity<>("Profile for user " + user.getEmail() +" not found", HttpStatus.NOT_FOUND);

        CohortResponse cohortResponse = new CohortResponse();
        Cohort cohort = teacherProfile.getCohort();
        cohortResponse.set(cohort);

        return new ResponseEntity<CohortResponse>(cohortResponse, HttpStatus.OK);
    }

    @PatchMapping("/teacher/{id}")
    public ResponseEntity<?> addStudentToCohort(@PathVariable int id, @RequestBody ProfileRequest profileRequest){
        Cohort cohort = cohortRepository.findById(id).orElse(null);
        if (cohort == null) return new ResponseEntity<>("Cohort for id " + Integer.valueOf(id) + " not found", HttpStatus.NOT_FOUND);

        Profile profile = profileRepository.findById(profileRequest.getUserId()).orElse(null);
        if (profile == null) return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);

        Cohort updatedCohort = cohortRepository.findById(profileRequest.getCohort()).orElse(null);
        if (updatedCohort == null) return new ResponseEntity<>("Cohort not found", HttpStatus.NOT_FOUND);

        profile.setCohort(updatedCohort);

        return new ResponseEntity<>(profileRepository.save(profile), HttpStatus.OK);
    }
}
