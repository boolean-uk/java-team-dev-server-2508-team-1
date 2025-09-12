package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {
    @NotBlank
    private String body;
    
    private int userId;
    
    public CommentRequest() {}
    
    public CommentRequest(String body, int userId) {
        this.body = body;
        this.userId = userId;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
