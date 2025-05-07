package com.backend.app.service;

import com.backend.app.dto.GroupRequest;
import com.backend.app.dto.GroupResponse;
import com.backend.app.dto.UserResponse;
import com.backend.app.entity.Group;
import com.backend.app.entity.User;
import com.backend.app.repository.GroupRepository;
import com.backend.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    public GroupResponse getGroupById(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new RuntimeException("Group not found"));
        return mapToGroupResponse(group);
    }

    public ResponseEntity<Map<String, Object>> createGroup(GroupRequest request) {
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Group savedGroup = groupRepository.save(group);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Group created successfully");
        response.put("group", mapToGroupResponse(savedGroup));

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> updateGroup(Long id, GroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        Group updatedGroup = groupRepository.save(group);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Group updated successfully");
        response.put("group", mapToGroupResponse(updatedGroup));

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> deleteGroup(Long id) {
        groupRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Group deleted successfully");
        return ResponseEntity.ok(response);
    }

    public GroupResponse addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        group.getUsers().add(user);
        return mapToGroupResponse(groupRepository.save(group));
    }

    public GroupResponse removeUserFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        group.getUsers().remove(user);
        return mapToGroupResponse(groupRepository.save(group));
    }

    private GroupResponse mapToGroupResponse(Group group) {
        List<UserResponse> userResponses = group.getUsers().stream().map(user ->
                new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getProfileImage()
                )
        ).collect(Collectors.toList());

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                userResponses
        );
    }
}
