package com.booleanuk.cohorts.payload.request;

import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.Profile;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CohortRequestWithProfiles {
    private String name;
    private int courseId;
    private List<Integer> profileIds;
    private String start_date;
    private String end_date;

    public CohortRequestWithProfiles(){}}

