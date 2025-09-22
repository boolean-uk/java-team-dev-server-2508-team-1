package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "cohorts")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties("cohorts")
    private Course course;

    @OneToMany(mappedBy = "cohort", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cohort")
    private List<Profile> profiles;


    public Cohort(int id) {
        this.id = id;
    }


    public Cohort(String name, List<Profile> profiles, Course course){
        this.name = name;
        this.profiles = profiles;
        this.course = course;
    }

    public Cohort(String name){
        this.name = name;
    }

    public Cohort(String name, Course course){
        this.name = name;
        this.course = course;
    }

    public Cohort(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString(){
        return "Cohort Id: " + this.id;
    }
}
