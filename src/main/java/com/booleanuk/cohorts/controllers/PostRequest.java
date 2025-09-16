package com.booleanuk.cohorts.payload.request;

public class PostRequest {
    private Integer userId;
    private String content;

    public PostRequest() {}

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
