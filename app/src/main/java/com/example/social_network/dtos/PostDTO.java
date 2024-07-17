package com.example.social_network.dtos;

import java.util.List;

public class PostDTO {

    private Long id;

    private Long userId;

    private String description;

    private String file;

    private String postedAt;

    private List<CommentDTO> comments;

    public PostDTO() {
    }

    public PostDTO(Long id, Long userId, String description, String file, String postedAt, List<CommentDTO> comments) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.file = file;
        this.postedAt = postedAt;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(String postedAt) {
        this.postedAt = postedAt;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }
}
