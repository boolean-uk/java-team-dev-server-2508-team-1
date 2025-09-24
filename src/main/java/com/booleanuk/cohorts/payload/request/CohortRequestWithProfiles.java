package com.booleanuk.cohorts.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CohortRequestWithProfiles {
    private String name;
    private int courseId;
    private String startDate;
    private String endDate;
    private List<Integer> profileIds;

    public CohortRequestWithProfiles(){}}

