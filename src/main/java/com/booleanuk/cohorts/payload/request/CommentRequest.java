package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentRequest {
    @NotBlank
    private String body;
    
    private int userId;
    
    public CommentRequest() {}
    
    public CommentRequest(String body, int userId) {
        this.body = body;
        this.userId = userId;
    }

}
