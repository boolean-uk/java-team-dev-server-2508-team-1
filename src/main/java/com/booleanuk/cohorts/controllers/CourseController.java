package com.booleanuk.cohorts.controllers;


import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.CourseRequest;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("courses")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @GetMapping
    public ResponseEntity<CourseListResponse> getAllCourse(){
        CourseListResponse courseListResponse = new CourseListResponse();
        courseListResponse.set(this.courseRepository.findAll());
        return ResponseEntity.ok(courseListResponse);
    }

    @GetMapping("students/{id}")
    public ResponseEntity<?> getAllStudents(@PathVariable int id){
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return new ResponseEntity<>("Course not found", HttpStatus.NOT_FOUND);

        List<Cohort> cohorts = course.getCohorts();
        List<Profile> profiles = new ArrayList<>();
        for (Cohort cohort : cohorts) {
            profiles.addAll(cohort.getProfiles());
        }
        List<Profile> students = profiles.stream().filter(it -> it.getRole().getId() == 2).toList();
        ProfileListResponse profileListResponse = new ProfileListResponse();
        profileListResponse.set(students);
        return ResponseEntity.ok(profileListResponse);
    }

    @GetMapping("{id}")
    public ResponseEntity<Response> getCourseById(@PathVariable int id){
        Course course = this.courseRepository.findById(id).orElse(null);
        if (course == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        CourseResponse courseResponse = new CourseResponse();
        courseResponse.set(course);
        return ResponseEntity.ok(courseResponse);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest courseRequest){
        Course course = new Course();
        String startDate = courseRequest.getStartDate();
        String endDate = courseRequest.getEndDate();


        if (startDate.isBlank() || endDate.isBlank())
            return new ResponseEntity<>("Date cannot be blank", HttpStatus.BAD_REQUEST);

        course.setName(courseRequest.getName());
        course.setStartDate(LocalDate.parse(courseRequest.getStartDate()));
        course.setEndDate(LocalDate.parse(courseRequest.getEndDate()));
        Course saveCourse = this.courseRepository.save(course);
        CourseResponse courseResponse = new CourseResponse();
        courseResponse.set(saveCourse);
        return new ResponseEntity<>(courseResponse, HttpStatus.CREATED);

    }

}
