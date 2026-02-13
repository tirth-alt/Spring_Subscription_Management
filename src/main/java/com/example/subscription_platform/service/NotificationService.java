package com.example.subscription_platform.service;

// import com.example.subscription_platform.model.Subscription;
// import com.example.subscription_platform.model.SubscriptionPlan;
// import com.example.subscription_platform.model.User;
import com.example.subscription_platform.repository.SubscriptionPlanRepository;
import com.example.subscription_platform.repository.SubscriptionRepository;
import com.example.subscription_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending notifications about subscription events.
 * Integrates with EmailService to send event-based notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Send notification when user registers.
     */
    @Async
    public void onUserRegistered(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            log.info("Sending welcome email to user: {}", user.getEmail());
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        });
    }

    /**
     * Send notification when subscription is activated.
     */
    @Async
    public void onSubscriptionActivated(String subscriptionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            userRepository.findById(subscription.getUserId()).ifPresent(user -> {
                planRepository.findById(subscription.getPlanId()).ifPresent(plan -> {
                    log.info("Sending subscription confirmation to user: {}", user.getEmail());

                    String endDate = subscription.getEndDate().format(DATE_FORMATTER);
                    emailService.sendSubscriptionConfirmation(
                            user.getEmail(),
                            user.getFirstName(),
                            plan.getName(),
                            endDate);
                });
            });
        });
    }

    /**
     * Send notification when subscription is about to expire (7 days before).
     */
    @Async
    public void onSubscriptionExpiringSoon(String subscriptionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            userRepository.findById(subscription.getUserId()).ifPresent(user -> {
                planRepository.findById(subscription.getPlanId()).ifPresent(plan -> {
                    log.info("Sending expiry reminder to user: {}", user.getEmail());

                    String expiryDate = subscription.getEndDate().format(DATE_FORMATTER);
                    emailService.sendExpiryReminder(
                            user.getEmail(),
                            user.getFirstName(),
                            plan.getName(),
                            expiryDate);
                });
            });
        });
    }

    /**
     * Send notification when payment is successful.
     */
    @Async
    public void onPaymentSuccess(String userId, String amount, String planName, String invoiceUrl) {
        userRepository.findById(userId).ifPresent(user -> {
            log.info("Sending payment receipt to user: {}", user.getEmail());

            emailService.sendPaymentReceipt(
                    user.getEmail(),
                    user.getFirstName(),
                    amount,
                    planName,
                    invoiceUrl != null ? invoiceUrl : "http://localhost:8080/api/payments/invoice");
        });
    }

    /**
     * Send notification when subscription is cancelled.
     */
    @Async
    public void onSubscriptionCancelled(String subscriptionId) {
        // TODO: Implement cancellation email
        log.info("Subscription cancelled notification for: {}", subscriptionId);
    }

    /**
     * Send notification when payment fails.
     */
    @Async
    public void onPaymentFailed(String userId, String reason) {
        // TODO: Implement payment failure email
        log.info("Payment failed notification for user: {}, reason: {}", userId, reason);
    }
}
