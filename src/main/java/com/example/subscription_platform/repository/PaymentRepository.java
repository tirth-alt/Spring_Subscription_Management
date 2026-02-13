package com.example.subscription_platform.repository;

import com.example.subscription_platform.model.Payment;
import com.example.subscription_platform.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity.
 * Provides MongoDB CRUD operations and custom queries.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Find all payments by user ID.
     * 
     * @param userId User ID
     * @return List of user's payments
     */
    List<Payment> findByUserId(String userId);

    /**
     * Find payments by user ID with pagination.
     * Used for payment history.
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of user's payments
     */
    Page<Payment> findByUserId(String userId, Pageable pageable);

    /**
     * Find payments by subscription ID.
     * Used to view all payments for a subscription.
     * 
     * @param subscriptionId Subscription ID
     * @return List of payments for the subscription
     */
    List<Payment> findBySubscriptionId(String subscriptionId);

    /**
     * Find payment by Razorpay order ID.
     * Used for payment verification and webhook processing.
     * 
     * @param razorpayOrderId Razorpay order ID
     * @return Optional containing payment if found
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Find payment by Razorpay payment ID.
     * Used for payment tracking.
     * 
     * @param razorpayPaymentId Razorpay payment ID
     * @return Optional containing payment if found
     */
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Find payments by user ID and status.
     * Used for filtering payment history.
     * 
     * @param userId User ID
     * @param status Payment status
     * @return List of matching payments
     */
    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);

    /**
     * Calculate total revenue from successful payments.
     * Used for analytics dashboard.
     * 
     * @param status Payment status (SUCCESS)
     * @return Total revenue
     */
    @Query(value = "{ 'status': ?0 }", fields = "{ 'amount': 1 }")
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find successful payments within date range.
     * Used for revenue analytics and reporting.
     * 
     * @param status    Payment status (SUCCESS)
     * @param startDate Start date
     * @param endDate   End date
     * @return List of successful payments in range
     */
    @Query("{ 'status': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<Payment> findSuccessfulPaymentsInDateRange(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
