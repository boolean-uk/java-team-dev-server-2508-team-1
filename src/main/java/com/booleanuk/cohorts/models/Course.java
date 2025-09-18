package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@Data
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @ManyToMany(mappedBy = "cohort_courses")
    @JsonIgnoreProperties("cohort_courses")
    private List<Cohort> cohorts;

    public Course(String name) {
        this.name = name;
    }

    @Override
    public String toString(){

        return "Course Name: " + this.name;
    }

}
