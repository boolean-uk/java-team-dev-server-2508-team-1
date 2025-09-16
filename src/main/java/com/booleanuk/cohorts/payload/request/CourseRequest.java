package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;

public class CourseRequest {
    @NotBlank
    private String name;

    public CourseRequest() {}

    public CourseRequest(String name){
        this.name = name;
    }

    public String getName() { return name; }


}
