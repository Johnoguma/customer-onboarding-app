package com.backend.app.dto;

import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private List<UserResponse> users;

    // Constructor
    public GroupResponse(Long id, String name, String description, List<UserResponse> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.users = users;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<UserResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }
}
// This class is used to transfer data for a group, including its ID, name, description, and a list of users in the group.
// It is used in the response of the GroupController to provide detailed information about a group.