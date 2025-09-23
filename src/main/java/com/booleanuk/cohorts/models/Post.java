package com.booleanuk.cohorts.models;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"user", "comments"})
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int likes = 0;

    @Column(name = "time_created", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timeCreated;

    @Column(name = "time_updated", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timeUpdated;

    @ManyToMany(mappedBy = "likedPosts")
    private Set<User> likedByUsers = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties(value = {"posts", "comments", "cohort", "roles", "likedPosts"})
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("post")
    private List<Comment> comments;

    @Transient
    private Author author;

    public Post(int id) {
        this.id = id;
        this.likes = 0;
    }

    public Post(User user, String content) {
        this.user = user;
        this.content = content;
        this.likes = 0;
    }

    public Post(Author author, String content) {
        this.author = author;
        this.content = content;
        this.likes = 0;
    }

    public Post(String content, User user, List<Comment> comments) {
        this.content = content;
        this.user = user;
        this.comments = comments;
        this.likes = 0;
    }

    public Post(String content, User user, int likes) {
        this.content = content;
        this.user = user;
        this.likes = likes;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.timeCreated = now;
        this.timeUpdated = now;
    }



}
