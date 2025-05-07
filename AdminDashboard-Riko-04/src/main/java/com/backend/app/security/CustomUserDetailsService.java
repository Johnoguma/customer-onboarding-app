package com.backend.app.security;

import com.backend.app.entity.User;
import com.backend.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.AuthorityUtils; // Import AuthorityUtils
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = user.getRole().replace("ROLE_", "").toUpperCase();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),  // Use email as the username here
                user.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_" + role)
        );
    }
}
