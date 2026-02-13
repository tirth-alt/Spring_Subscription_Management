package com.example.subscription_platform.dto.user;

import com.example.subscription_platform.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user information.
 * Does not expose sensitive information like password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
}
