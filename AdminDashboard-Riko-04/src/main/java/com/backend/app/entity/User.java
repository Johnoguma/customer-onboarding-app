package com.backend.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String role;

    private String profileImage;

    @Builder.Default
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();
}
