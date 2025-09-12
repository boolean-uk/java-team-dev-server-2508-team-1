package com.booleanuk.cohorts.payload.response;

import com.booleanuk.cohorts.models.Comment;

import lombok.Getter;

@Getter
public class CommentResponse extends Response {

    public void set(Comment comment) {
        Data<Comment> data = new CommentData();
        data.set(comment);
        super.set(data);
    }
}
