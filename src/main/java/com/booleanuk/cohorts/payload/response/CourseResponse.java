package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Course;
import lombok.Getter;

@Getter
public class CourseResponse extends Response{
    public void set(Course course){
        Data<Course> data = new CourseData();
        data.set(course);
        super.set(data);
    }
}