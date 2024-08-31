package com.example.social_network.dtos;

public class NewPostDTO {

    private String description;

    public NewPostDTO(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

