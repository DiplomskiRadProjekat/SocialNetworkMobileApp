package com.example.social_network.dtos;

import java.io.File;

public class NewPostDTO {

    private String description;

    private File file;

    public NewPostDTO(String description, File file) {
        this.description = description;
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}

