package com.booleanuk.cohorts.repository;

import com.booleanuk.cohorts.models.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Integer> {
}
