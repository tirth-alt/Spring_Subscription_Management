package com.example.subscription_platform.repository;

import com.example.subscription_platform.model.Subscription;
import com.example.subscription_platform.model.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subscription entity.
 * Provides MongoDB CRUD operations, custom queries, and aggregations.
 */
@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    /**
     * Find all subscriptions for a user.
     * 
     * @param userId User ID
     * @return List of user's subscriptions
     */
    List<Subscription> findByUserId(String userId);

    /**
     * Find all subscriptions for a user with pagination.
     * Used for subscription history.
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of user's subscriptions
     */
    Page<Subscription> findByUserId(String userId, Pageable pageable);

    /**
     * Find subscriptions by user ID and status.
     * Used for filtering user's subscriptions.
     * 
     * @param userId User ID
     * @param status Subscription status
     * @return List of matching subscriptions
     */
    List<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    /**
     * Find active subscription for a user and plan.
     * Business rule: One active subscription per user per plan.
     * 
     * @param userId User ID
     * @param planId Plan ID
     * @param status Subscription status
     * @return Optional containing subscription if found
     */
    Optional<Subscription> findByUserIdAndPlanIdAndStatus(String userId, String planId, SubscriptionStatus status);

    /**
     * Find all active subscriptions.
     * Used for analytics and scheduled tasks (auto-expiry).
     * 
     * @return List of active subscriptions
     */
    List<Subscription> findByStatus(SubscriptionStatus status);

    /**
     * Find subscriptions that have expired but status not yet updated.
     * Used by scheduled task to auto-expire subscriptions.
     * 
     * @param status Current status (ACTIVE)
     * @param now    Current timestamp
     * @return List of expired subscriptions
     */
    @Query("{ 'status': ?0, 'endDate': { $lt: ?1 } }")
    List<Subscription> findExpiredSubscriptions(SubscriptionStatus status, LocalDateTime now);

    /**
     * Count active subscriptions.
     * Used for analytics dashboard.
     * 
     * @param status Subscription status (ACTIVE)
     * @return Count of active subscriptions
     */
    long countByStatus(SubscriptionStatus status);

    /**
     * Find all subscriptions for a specific plan.
     * Used for plan popularity analytics.
     * 
     * @param planId Plan ID
     * @return List of subscriptions for the plan
     */
    List<Subscription> findByPlanId(String planId);

    /**
     * Count subscriptions by plan ID and status.
     * Used for analytics (e.g., active subscribers per plan).
     * 
     * @param planId Plan ID
     * @param status Subscription status
     * @return Count of subscriptions
     */
    long countByPlanIdAndStatus(String planId, SubscriptionStatus status);
}
