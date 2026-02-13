package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.auth.AuthResponse;
import com.example.subscription_platform.dto.auth.LoginRequest;
import com.example.subscription_platform.dto.auth.RegisterRequest;
import com.example.subscription_platform.exception.DuplicateResourceException;
import com.example.subscription_platform.model.Role;
import com.example.subscription_platform.model.User;
import com.example.subscription_platform.repository.UserRepository;
import com.example.subscription_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 * Handles user registration and login with JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     * 
     * @param request Registration details
     * @return Authentication response with JWT token
     * @throws DuplicateResourceException if email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER) // Default role
                .build();

        // Save user
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Build response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // Get authenticated user
        User user = (User) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Build response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
