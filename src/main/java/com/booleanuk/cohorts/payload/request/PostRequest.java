package com.booleanuk.cohorts.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostRequest {
    @NotBlank
    private String content;
    
    private int userId;
    
    public PostRequest() {}
    
    public PostRequest(String content, int userId) {
        this.content = content;
        this.userId = userId;
    }
}
