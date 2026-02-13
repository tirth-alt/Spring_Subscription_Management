package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.user.UserDTO;
import com.example.subscription_platform.dto.user.UserUpdateRequest;
import com.example.subscription_platform.exception.ResourceNotFoundException;
import com.example.subscription_platform.model.User;
import com.example.subscription_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user management operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get currently authenticated user.
     * 
     * @return Current user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        return (User) authentication.getPrincipal();
    }

    /**
     * Get current user profile.
     * 
     * @return User DTO
     */
    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToDTO(user);
    }

    /**
     * Update current user profile.
     * 
     * @param request Update request
     * @return Updated user DTO
     */
    public UserDTO updateCurrentUserProfile(UserUpdateRequest request) {
        User user = getCurrentUser();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return mapToDTO(user);
    }

    /**
     * Get user by ID (admin only).
     * 
     * @param userId User ID
     * @return User DTO
     */
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToDTO(user);
    }

    /**
     * Get all users (admin only).
     * 
     * @return List of user DTOs
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map User entity to UserDTO.
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
