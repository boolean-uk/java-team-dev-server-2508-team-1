package com.booleanuk.cohorts.controllers;


import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.Post;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.CourseRequest;
import com.booleanuk.cohorts.payload.request.PostRequest;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("courses")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<CourseListResponse> getAllCourse(){
        CourseListResponse courseListResponse = new CourseListResponse();
        courseListResponse.set(this.courseRepository.findAll());
        return ResponseEntity.ok(courseListResponse);
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
