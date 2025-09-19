package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIncludeProperties("id")
    private List<Cohort> cohorts;

    public Course(String name) {
        this.name = name;
    }

    public Course(String name, List<Cohort> cohorts){
        this.name = name;
        this.cohorts = cohorts;
    }

    @Override
    public String toString(){

        return "Course Name: " + this.name;
    }

}
