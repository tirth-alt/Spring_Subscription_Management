package com.example.subscription_platform.repository;

import com.example.subscription_platform.model.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SubscriptionPlan entity.
 * Provides MongoDB CRUD operations and custom queries.
 */
@Repository
public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {

    /**
     * Find all active subscription plans.
     * Used for public plan listings.
     * 
     * @return List of active plans
     */
    List<SubscriptionPlan> findByActiveTrue();

    /**
     * Find all active plans with pagination.
     * Used for paginated public plan listings.
     * 
     * @param pageable Pagination parameters
     * @return Page of active plans
     */
    Page<SubscriptionPlan> findByActiveTrue(Pageable pageable);

    /**
     * Find plan by name.
     * Used for duplicate name validation.
     * 
     * @param name Plan name
     * @return Optional containing plan if found
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Check if plan exists with given name.
     * Used for uniqueness validation during creation/update.
     * 
     * @param name Plan name
     * @return true if plan exists, false otherwise
     */
    boolean existsByName(String name);
}
