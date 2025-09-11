package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.Post;
import lombok.Getter;

import java.util.List;

@Getter
public class CourseListResponse extends Response {
    public void set(List<Course> courses) {
        Data<List<Course>> data = new CourseListData();
        data.set(courses);
        super.set(data);
    }
}
