package com.backend.app.controller;

import com.backend.app.dto.AuthRequest;
import com.backend.app.dto.AuthResponse;
import com.backend.app.entity.User;
import com.backend.app.repository.UserRepository;
import com.backend.app.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        // Check if the user already exists by username or email
        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User already exists"));
        }

        // Create a new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encode the password
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER") // Default role is "CUSTOMER"
                .profileImage(request.getProfileImage()) // Set profile image URL
                .build();

        // Save the user to the repository
        userRepository.save(user);

        // Return success message as a JSON object
        return ResponseEntity.ok(Collections.singletonMap("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtUtil.getExpiration() / 1000));

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (jwtUtil.getRefreshExpiration() / 1000));

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 💡 Return role explicitly for frontend redirection
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                     HttpServletResponse response) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String userEmail = jwtUtil.extractUserId(refreshToken); // Now returns email
        String newAccessToken = jwtUtil.generateToken(userEmail);

        // Set the new access token in the cookie
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtUtil.getExpiration() / 1000));
        response.addCookie(accessCookie);

        return ResponseEntity.ok(Collections.singletonMap("message", "Access token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear cookies
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Collections.singletonMap("message", "Logout successful"));
    }
}
