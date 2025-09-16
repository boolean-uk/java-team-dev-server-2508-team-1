package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Course;
import lombok.Getter;


@Getter
public class CourseData extends Data<Course> {
    protected Course course;

    @Override
    public void set(Course course) {
        this.course = course;
    }
}
