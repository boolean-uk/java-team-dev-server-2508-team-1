package com.booleanuk.cohorts.payload.request;



import com.booleanuk.cohorts.models.Cohort;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {
    private int cohort;
    private int userId;

    public ProfileRequest(){}

    public int getCohort() { return cohort; }
    public int getUserId() { return userId; }
}
