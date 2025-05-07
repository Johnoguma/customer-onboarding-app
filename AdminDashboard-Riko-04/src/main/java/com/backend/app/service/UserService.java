package com.backend.app.service;

import com.backend.app.dto.UserRequest;
import com.backend.app.dto.UserResponse;
import com.backend.app.entity.User;
import com.backend.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(this::convertToUserResponse).orElse(null);
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        return userRepository.existsByUsernameOrEmail(username, email);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public UserResponse createUser(UserRequest userRequest) {
        // Check if username or email already exists
        if (existsByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail())) {
            throw new RuntimeException("Username or email already exists.");
        }

        // Create user entity
        User user = convertToUser(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Encrypt password

        // Save user to the database
        User savedUser = userRepository.save(user);

        // Return the user response (without password)
        return convertToUserResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserRequest updatedUserRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return null;  // User not found
        }

        User user = userOpt.get();
        user.setUsername(updatedUserRequest.getUsername() != null ? updatedUserRequest.getUsername() : user.getUsername());
        user.setEmail(updatedUserRequest.getEmail() != null ? updatedUserRequest.getEmail() : user.getEmail());
        user.setRole(updatedUserRequest.getRole() != null ? updatedUserRequest.getRole() : user.getRole());
        user.setProfileImage(updatedUserRequest.getProfileImage() != null ? updatedUserRequest.getProfileImage() : user.getProfileImage());

        userRepository.save(user);
        return convertToUserResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getProfileImage());
    }

    private User convertToUser(UserRequest userRequest) {
        return User.builder()
                   .username(userRequest.getUsername())
                   .email(userRequest.getEmail())
                   .password(userRequest.getPassword()) 
                   .role(userRequest.getRole())
                   .profileImage(userRequest.getProfileImage())
                   .build();
    }
}
