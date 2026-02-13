package com.example.subscription_platform.dto.auth;

import com.example.subscription_platform.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication operations (login/register).
 * Contains JWT token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
