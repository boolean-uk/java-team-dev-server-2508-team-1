package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;

public class PostRequest {
    @NotBlank
    private String content;
    
    private int userId;
    
    public PostRequest() {}
    
    public PostRequest(String content, int userId) {
        this.content = content;
        this.userId = userId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
