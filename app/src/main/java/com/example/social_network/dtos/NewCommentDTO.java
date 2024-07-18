package com.example.social_network.dtos;

public class NewCommentDTO {

    private Long userId;

    private String comment;

    private Long parentCommentId;

    public NewCommentDTO() {
    }

    public NewCommentDTO(Long userId, String comment, Long parentCommentId) {
        this.userId = userId;
        this.comment = comment;
        this.parentCommentId = parentCommentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

}
