package com.example.subscription_platform.repository;

import com.example.subscription_platform.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides MongoDB CRUD operations and custom queries.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address.
     * Used for authentication and uniqueness validation.
     * 
     * @param email User's email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists with given email.
     * Used for registration validation.
     * 
     * @param email Email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Count users registered within a date range.
     * Used for analytics (new users per month).
     * 
     * @param startDate Start of date range
     * @param endDate   End of date range
     * @return Count of users registered in the range
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count all active users.
     * Used for analytics dashboard.
     * 
     * @param active Active status
     * @return Count of active users
     */
    long countByActive(Boolean active);
}
