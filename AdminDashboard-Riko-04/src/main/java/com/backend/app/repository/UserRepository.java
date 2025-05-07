package com.backend.app.repository;

import com.backend.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByUsernameOrEmail(String username, String email);
}
