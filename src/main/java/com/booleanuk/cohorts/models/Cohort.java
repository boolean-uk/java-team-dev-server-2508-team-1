package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Data
@Entity
@Table(name = "cohorts")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @ManyToMany
    @JsonIgnoreProperties("cohorts")
    @JoinTable(name = "cohort_course",
            joinColumns = @JoinColumn(name = "cohort_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> cohort_courses;


    public Cohort(int id) {
        this.id = id;
    }

    @Override
    public String toString(){

        return "Cohort Id: " + this.id;
    }
}
