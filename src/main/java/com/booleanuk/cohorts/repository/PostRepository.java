package com.booleanuk.cohorts.repository;

import com.booleanuk.cohorts.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH u.profile pr LEFT JOIN FETCH pr.cohort WHERE p.id = :id")
    Optional<Post> findByIdWithUser(@Param("id") int id);
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH u.profile pr LEFT JOIN FETCH pr.cohort")
    List<Post> findAllWithUsers();
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_liked_posts WHERE post_id = :postId", nativeQuery = true)
    void removeLikedPostFromAllUsers(@Param("postId") int postId);
}
