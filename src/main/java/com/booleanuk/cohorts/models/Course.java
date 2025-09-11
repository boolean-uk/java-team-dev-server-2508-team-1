package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    @JsonIgnoreProperties("cohort_courses")
    @ManyToMany(mappedBy = "cohort_courses")
    private List<Cohort> cohorts;

    public Course(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString(){

        return "Course Name: " + this.name;
    }

}
