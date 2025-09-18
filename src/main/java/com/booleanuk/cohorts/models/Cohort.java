package com.booleanuk.cohorts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    @Override
    public String toString(){

        return "Cohort Id: " + this.id;
    }
}
