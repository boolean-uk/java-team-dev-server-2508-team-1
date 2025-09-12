package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Comment;

import lombok.Getter;

@Getter
public class CommentData extends Data<Comment> {
    protected Comment comment;

    @Override
    public void set(Comment comment) {
        this.comment = comment;
    }
}
