package com.backend.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extract token from cookies
        String token = extractTokenFromCookies(request);

        // Validate and authenticate user if token is valid
        if (token != null && jwtUtil.validateToken(token) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(request, token);
        }

        // Pass request along the filter chain
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        // Find token in cookies
        Optional<Cookie> tokenCookie = 
            java.util.Arrays.stream(cookies)
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .findFirst();

        return tokenCookie.map(Cookie::getValue).orElse(null);
    }

    private void authenticateUser(HttpServletRequest request, String token) {
        String email = jwtUtil.extractUserId(token); // This returns the email from the token

        if (email != null) {
            var userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("User authenticated: " + email); // Log authentication
            } else {
                logger.warning("User details not found for email: " + email); // Log if user not found
            }
        } else {
            logger.warning("Invalid token: Could not extract email"); // Log invalid token
        }
    }
}
