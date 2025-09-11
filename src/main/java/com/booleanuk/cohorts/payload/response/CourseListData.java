package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.Post;
import lombok.Getter;

import java.util.List;

@Getter
public class CourseListData extends Data<List<Course>> {

    protected List<Course> courses;

    @Override
    public void set(List<Course> courseList) {
        this.courses = courseList;
    }

}