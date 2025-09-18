package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/*
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
*/

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

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    public Cohort(int id) {
        this.id = id;
    }

    public Cohort(LocalDate startDate, LocalDate endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Cohort(String name, LocalDate startDate, LocalDate endDate){
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Cohort(int id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toString(){
        return "Cohort Id: " + this.id;
    }
}
