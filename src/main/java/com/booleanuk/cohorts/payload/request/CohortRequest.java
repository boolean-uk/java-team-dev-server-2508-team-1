package com.booleanuk.cohorts.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CohortRequest {
    private String course;
    private String start_date;
    private String end_date;

    public CohortRequest(){}

    public String getCourse() { return course; }

    public String getStart_date() { return start_date; }

    public String getEnd_date() { return end_date; }

}
