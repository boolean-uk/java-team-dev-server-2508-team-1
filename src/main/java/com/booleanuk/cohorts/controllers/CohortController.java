package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.CohortRequest;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.CourseRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("cohorts")
public class CohortController {
    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CourseRepository courseRepository;

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
    public ResponseEntity<?> getCohortByUserId(@PathVariable int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return new ResponseEntity<>("User for id " + id + " not found", HttpStatus.NOT_FOUND);
        Profile teacherProfile = profileRepository.findById(user.getProfile().getId()).orElse(null);
        if (teacherProfile == null) return new ResponseEntity<>("Profile for user " + user.getEmail() +" not found", HttpStatus.NOT_FOUND);

        CohortResponse cohortResponse = new CohortResponse();
        Cohort cohort = teacherProfile.getCohort();
        cohortResponse.set(cohort);

        return new ResponseEntity<>(cohortResponse, HttpStatus.OK);
    }


    @PatchMapping("{id}")
    public ResponseEntity<?> editCohortById(@PathVariable int id, @RequestBody CohortRequest cohortRequest){
        Cohort cohort = cohortRepository.findById(id).orElse(null);
        if (cohort == null){
            return new ResponseEntity<>("Cohort not found", HttpStatus.NOT_FOUND);
        }

        List<Profile> profilesToInclude = cohortRequest.getProfileIds().stream()
                .map(profileId -> profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile with id " + profileId + " not found")))
                .toList();

        List<Course> courses = cohortRequest.getCourseIds().stream()
                .map(courseId -> courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Profile with id " + courseId + " not found")))
                .collect(Collectors.toList());

        if (cohortRequest.getName().isBlank()) {
            return new ResponseEntity<>("Name cannot be blank", HttpStatus.BAD_REQUEST);
        }

        if (cohortRequest.getStart_date().isBlank() || cohortRequest.getEnd_date().isBlank()) {
            return new ResponseEntity<>("Date cannot be blank", HttpStatus.BAD_REQUEST);
        }

        cohort.setCohort_courses(courses);

        List<User> usersToInclude = userRepository.findAll().stream().filter(it ->
                profilesToInclude.contains(it.getProfile())).toList();

        List<User> usersToExclude = userRepository.findAll().stream().filter(it ->
                it.getCohort().getId() == cohort.getId() && !(profilesToInclude.contains(it.getProfile()))).toList();

        List<Profile> profilesToExclude = usersToExclude.stream().map(User::getProfile).toList().stream().filter(it ->
                it.getCohort().getId() == cohort.getId() && !(profilesToInclude.contains(it))).toList();

        cohort.setName(cohortRequest.getName());
        cohort.setStartDate(LocalDate.parse(cohortRequest.getStart_date()));
        cohort.setEndDate(LocalDate.parse(cohortRequest.getEnd_date()));

        Cohort cohortRes = cohortRepository.findById(99).orElse(null);
        if (cohortRes == null) {
            return new ResponseEntity<>("Could not find RESERVE", HttpStatus.BAD_REQUEST);
        }

        for (User user: usersToExclude){
            user.setCohort(cohortRes);
        }

        for (Profile profile: profilesToExclude){
            profile.setCohort(cohortRes);
            List<Profile> prevProf = cohortRes.getProfiles();
            prevProf.add(profile);
            cohortRes.setProfiles(prevProf);
        }

        for (User user: usersToInclude){
            user.setCohort(cohort);
        }
        for (Profile prof : profilesToInclude){
            prof.setCohort(cohort);
        }
        profileRepository.saveAll(profilesToInclude);
        profileRepository.saveAll(profilesToExclude);
        userRepository.saveAll(usersToInclude);
        userRepository.saveAll(usersToExclude);
        cohortRepository.save(cohortRes);

        return new ResponseEntity<>(cohortRepository.save(cohort), HttpStatus.OK);
    }
}

