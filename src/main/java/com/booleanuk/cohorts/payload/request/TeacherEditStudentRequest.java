package com.booleanuk.cohorts.payload.request;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.Role;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TeacherEditStudentRequest {


    private int role_id;
    private int cohort_id;
    private LocalDate start_date;
    private LocalDate end_date;
}
