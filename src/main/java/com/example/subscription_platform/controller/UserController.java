package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.dto.user.UserDTO;
import com.example.subscription_platform.dto.user.UserUpdateRequest;
import com.example.subscription_platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user management endpoints.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile.
     * 
     * @return User profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get authenticated user's profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        UserDTO user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update current user profile.
     * 
     * @param request Update request
     * @return Updated profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Update authenticated user's profile (firstName, lastName)")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        UserDTO user = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    /**
     * Get all users (admin only).
     * 
     * @return List of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get list of all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID (admin only).
     * 
     * @param userId User ID
     * @return User details
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
