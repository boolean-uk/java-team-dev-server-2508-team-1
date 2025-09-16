package com.booleanuk.cohorts.repository;

import com.booleanuk.cohorts.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Integer> {
}
