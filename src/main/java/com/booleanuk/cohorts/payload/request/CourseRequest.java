package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CourseRequest {
    @NotBlank
    private String name;
    private String startDate;
    private String endDate;

    public CourseRequest() {}

    public CourseRequest(String name, String startDate, String endDate){
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }


}
