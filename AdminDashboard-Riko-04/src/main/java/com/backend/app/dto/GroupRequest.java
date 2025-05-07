package com.backend.app.dto;

import jakarta.validation.constraints.NotBlank;

public class GroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;

    // Constructor
    public GroupRequest() {}

    public GroupRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
// This class is used to transfer data for creating or updating a group.
// It includes validation annotations to ensure that the group name is provided.