package com.example.social_network.dtos;

import java.util.List;

public class CommentDTO {

    private Long id;

    private Long postId;

    private Long userId;

    private String comment;

    private String commentedAt;

    private List<CommentDTO> replies;

    private Long parentCommentId;

    public CommentDTO() {
    }

    public CommentDTO(Long id, Long postId, Long userId, String comment, String commentedAt, List<CommentDTO> replies, Long parentCommentId) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.comment = comment;
        this.commentedAt = commentedAt;
        this.replies = replies;
        this.parentCommentId = parentCommentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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

    public String getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(String commentedAt) {
        this.commentedAt = commentedAt;
    }

    public List<CommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentDTO> replies) {
        this.replies = replies;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}
