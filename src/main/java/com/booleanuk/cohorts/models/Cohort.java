package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)

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


    @OneToMany(mappedBy = "cohort", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cohort")
    private List<Profile> profiles;



    public Cohort(int id) {
        this.id = id;
    }

    @Override
    public String toString(){

        return "Cohort Id: " + this.id;
    }
}
