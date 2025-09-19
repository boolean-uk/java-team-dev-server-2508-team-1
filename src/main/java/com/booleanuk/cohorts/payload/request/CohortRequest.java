package com.booleanuk.cohorts.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CohortRequest {
    private String name;
    private int courseId;
    private String startDate;
    private String endDate;

    public CohortRequest(){}}
