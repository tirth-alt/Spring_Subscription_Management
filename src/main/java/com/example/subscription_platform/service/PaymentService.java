package com.example.subscription_platform.service;

import com.example.subscription_platform.dto.payment.PaymentRequest;
import com.example.subscription_platform.dto.payment.PaymentResponse;
import com.example.subscription_platform.dto.payment.PaymentVerificationRequest;
import com.example.subscription_platform.exception.ResourceNotFoundException;
import com.example.subscription_platform.model.Payment;
import com.example.subscription_platform.model.PaymentStatus;
import com.example.subscription_platform.model.Subscription;
import com.example.subscription_platform.model.User;
import com.example.subscription_platform.repository.PaymentRepository;
import com.example.subscription_platform.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for payment processing.
 * Integrates with Razorpay payment gateway.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final RazorpayService razorpayService;

    /**
     * Create payment order using Razorpay.
     * 
     * @param request Payment request
     * @return Payment response with Razorpay order ID
     */
    public PaymentResponse createPaymentOrder(PaymentRequest request) {
        User currentUser = userService.getCurrentUser();

        // Verify subscription exists and belongs to user
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", request.getSubscriptionId()));

        if (!subscription.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only create payments for your own subscriptions");
        }

        // Convert amount to paise (Razorpay expects amount in smallest currency unit)
        Integer amountInPaise = request.getAmount().multiply(new java.math.BigDecimal(100)).intValue();

        // Create order in Razorpay
        String razorpayOrderId = razorpayService.createOrder(
                amountInPaise,
                request.getCurrency(),
                "sub_" + subscription.getId().substring(0, 8));

        // Create payment record
        Payment payment = Payment.builder()
                .userId(currentUser.getId())
                .subscriptionId(subscription.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrderId)
                .build();

        paymentRepository.save(payment);

        log.info("Created Razorpay order {} for subscription {}", razorpayOrderId, subscription.getId());

        return mapToResponse(payment);
    }

    /**
     * Verify payment signature from Razorpay.
     * 
     * @param request Payment verification request
     * @return Payment response
     */
    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        // Find payment by order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", request.getRazorpayOrderId()));

        // Verify signature using Razorpay service
        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Payment signature verification failed for order {}", request.getRazorpayOrderId());
            throw new RuntimeException("Invalid payment signature");
        }

        // Update payment status
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentMethod("razorpay");
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // Activate subscription
        subscriptionService.activateSubscription(payment.getSubscriptionId());

        log.info("Payment {} verified successfully for subscription {}",
                payment.getId(), payment.getSubscriptionId());

        return mapToResponse(payment);
    }

    /**
     * Get payment by ID.
     * 
     * @param paymentId Payment ID
     * @return Payment response
     */
    public PaymentResponse getPaymentById(String paymentId) {
        User currentUser = userService.getCurrentUser();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        // Verify ownership
        if (!payment.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only view your own payments");
        }

        return mapToResponse(payment);
    }

    /**
     * Get payment history for current user.
     * 
     * @return List of payments
     */
    public List<PaymentResponse> getPaymentHistory() {
        User currentUser = userService.getCurrentUser();

        return paymentRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get payments for a subscription.
     * 
     * @param subscriptionId Subscription ID
     * @return List of payments
     */
    public List<PaymentResponse> getPaymentsBySubscription(String subscriptionId) {
        User currentUser = userService.getCurrentUser();

        // Verify subscription ownership
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        if (!subscription.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only view payments for your own subscriptions");
        }

        return paymentRepository.findBySubscriptionId(subscriptionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map payment entity to response DTO.
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .subscriptionId(payment.getSubscriptionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Get revenue statistics for admin dashboard.
     * 
     * @return Map containing revenue stats
     */
    public java.util.Map<String, Object> getRevenueStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try {
            // Get all successful payments
            List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

            // Calculate total revenue (with null safety)
            java.math.BigDecimal totalRevenue = successfulPayments.stream()
                    .map(Payment::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            // Get start and end of current month
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            // Calculate this month's revenue
            List<Payment> thisMonthPayments = paymentRepository.findSuccessfulPaymentsInDateRange(
                    PaymentStatus.SUCCESS, startOfMonth, endOfMonth);

            java.math.BigDecimal monthlyRevenue = thisMonthPayments.stream()
                    .map(Payment::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            // Count of successful payments
            long totalPayments = successfulPayments.size();
            long monthlyPayments = thisMonthPayments.size();

            stats.put("totalRevenue", totalRevenue);
            stats.put("monthlyRevenue", monthlyRevenue);
            stats.put("totalPayments", totalPayments);
            stats.put("monthlyPayments", monthlyPayments);

        } catch (Exception e) {
            log.error("Error calculating revenue stats", e);
            stats.put("totalRevenue", java.math.BigDecimal.ZERO);
            stats.put("monthlyRevenue", java.math.BigDecimal.ZERO);
            stats.put("totalPayments", 0L);
            stats.put("monthlyPayments", 0L);
        }

        return stats;
    }
}
