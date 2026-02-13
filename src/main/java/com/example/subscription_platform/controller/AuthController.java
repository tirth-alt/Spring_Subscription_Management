package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.auth.AuthResponse;
import com.example.subscription_platform.dto.auth.LoginRequest;
import com.example.subscription_platform.dto.auth.RegisterRequest;
import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 * Public endpoints - no authentication required.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authorization", description = "Authentication and registration APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     * 
     * @param request Registration details
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with USER role")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Login user.
     * 
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
