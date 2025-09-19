package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.CohortRequest;
import com.booleanuk.cohorts.payload.request.CohortRequestWithProfiles;
import com.booleanuk.cohorts.payload.request.ProfileRequest;
import com.booleanuk.cohorts.payload.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.CourseRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @PostMapping
    public ResponseEntity<?> addCohort(@RequestBody CohortRequest cohortRequest){

        Course course = courseRepository.findById(cohortRequest.getCourseId()).orElse(null);
        if (course == null) return new ResponseEntity<>("Course not found", HttpStatus.NOT_FOUND);

        String name = cohortRequest.getName();
        if (name.isBlank()) return new ResponseEntity<>("Name cannot be blank", HttpStatus.BAD_REQUEST);

        String startDate = cohortRequest.getStartDate();
        String endDate = cohortRequest.getEndDate();
        if (startDate.isBlank() || endDate.isBlank()) return new ResponseEntity<>("Date cannot be blank", HttpStatus.BAD_REQUEST);

        Cohort cohort = new Cohort(cohortRequest.getName(), course, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(cohortRepository.save(cohort));
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> editCohortById(@PathVariable int id, @RequestBody CohortRequestWithProfiles cohortRequest) {
        Cohort cohort = cohortRepository.findById(id).orElse(null);
        if (cohort == null) {
            return new ResponseEntity<>("Cohort not found", HttpStatus.NOT_FOUND);
        }
        Course course = courseRepository.findById(cohortRequest.getCourseId()).orElse(null);
        if (course == null) return new ResponseEntity<>("Course not found", HttpStatus.NOT_FOUND);

        String name = cohortRequest.getName();
        if (name.isBlank()) return new ResponseEntity<>("Name cannot be blank", HttpStatus.BAD_REQUEST);

        String startDate = cohortRequest.getStartDate();
        String endDate = cohortRequest.getEndDate();

        if (startDate.isBlank() || endDate.isBlank())
            return new ResponseEntity<>("Date cannot be blank", HttpStatus.BAD_REQUEST);

        List<Profile> profiles = profileRepository.findAll().stream().filter(it -> cohortRequest.getProfileIds().contains(it.getId())).collect(Collectors.toList());
        for (Profile oldProfile : new ArrayList<>(cohort.getProfiles())) {
            if (!profiles.contains(oldProfile)) {
                oldProfile.setCohort(null);
            }
        }

        for (Profile newProfile : profiles) {
            newProfile.setCohort(cohort);
        }

        cohort.setProfiles(profiles);
        cohort.setCourse(course);
        cohort.setName(cohortRequest.getName());
        cohort.setStartDate(LocalDate.parse(startDate));
        cohort.setEndDate(LocalDate.parse(endDate));

        return ResponseEntity.ok(cohortRepository.save(cohort));
    }

    @PatchMapping("/teacher/{id}")
    public ResponseEntity<?> addStudentToCohort(@PathVariable int id, @RequestBody ProfileRequest profileRequest){
        Cohort cohort = cohortRepository.findById(id).orElse(null);
        if (cohort == null) return new ResponseEntity<>("Cohort for id " + Integer.valueOf(id) + " not found", HttpStatus.NOT_FOUND);

        Profile profile = profileRepository.findById(profileRequest.getProfileId()).orElse(null);
        if (profile == null) return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);

        profile.setCohort(cohort);

        return new ResponseEntity<>(profileRepository.save(profile), HttpStatus.OK);
    }
}

