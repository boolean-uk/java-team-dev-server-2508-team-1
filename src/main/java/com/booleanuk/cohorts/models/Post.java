package com.booleanuk.cohorts.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)

@NoArgsConstructor
@AllArgsConstructor
@Data
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("users")
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
