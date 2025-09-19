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

        List<Cohort> cohorts = cohortRepository.findAll().stream().filter(it -> course.getCohorts().contains(it)).toList();
        List<Profile> profiles = new ArrayList<>();
        for (Cohort cohrt : cohorts) {
            profiles.addAll(cohrt.getProfiles());
        }
        List<Profile> students = profiles.stream().filter(it -> it.getRole().toString().equals("ROLE_STUDENT")).toList();
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
    public ResponseEntity<Response> createCourse(@RequestBody CourseRequest courseRequest){
        Course course = new Course(courseRequest.getName());
        Course saveCourse = this.courseRepository.save(course);
        CourseResponse courseResponse = new CourseResponse();
        courseResponse.set(saveCourse);
        return new ResponseEntity<>(courseResponse, HttpStatus.CREATED);

    }

}
