package com.booleanuk.cohorts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booleanuk.cohorts.models.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
