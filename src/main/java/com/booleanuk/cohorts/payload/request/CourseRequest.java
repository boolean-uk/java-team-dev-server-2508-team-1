package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CourseRequest {
    @NotBlank
    private String name;

    public CourseRequest() {}

    public CourseRequest(String name){
        this.name = name;
    }


}
