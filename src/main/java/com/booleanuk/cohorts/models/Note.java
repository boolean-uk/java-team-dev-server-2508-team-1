package com.booleanuk.cohorts.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Table(name = "notes")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private LocalDate created;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Note(User user, String title, String description) {
        this.user = user;
        this.title = title;
        this.description = description;
    }
}
