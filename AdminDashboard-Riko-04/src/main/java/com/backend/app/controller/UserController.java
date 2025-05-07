package com.backend.app.controller;

import com.backend.app.dto.UserRequest;
import com.backend.app.dto.UserResponse;
import com.backend.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUser(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(404).build();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        if (userService.existsByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail())) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        // Create the user and get the response
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(201).body(userResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest updatedUserRequest) {
        UserResponse updatedUser = userService.updateUser(id, updatedUserRequest);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.status(404).body("User not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }

        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }
}
