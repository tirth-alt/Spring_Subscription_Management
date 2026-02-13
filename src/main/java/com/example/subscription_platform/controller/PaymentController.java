package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.dto.payment.PaymentRequest;
import com.example.subscription_platform.dto.payment.PaymentResponse;
import com.example.subscription_platform.dto.payment.PaymentVerificationRequest;
import com.example.subscription_platform.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for payment operations.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payments", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create payment order.
     * 
     * @param request Payment request
     * @return Payment order details
     */
    @PostMapping("/create")
    @Operation(summary = "Create payment order", description = "Create payment order for subscription (Mock implementation)")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentOrder(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPaymentOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created successfully", payment));
    }

    /**
     * Verify payment (webhook from Razorpay).
     * 
     * @param request Payment verification request
     * @return Payment verification response
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verify payment signature (Mock implementation)")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        PaymentResponse payment = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
    }

    /**
     * Get payment by ID.
     * 
     * @param paymentId Payment ID
     * @return Payment details
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Get payment details by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable String paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Get payment history for current user.
     * 
     * @return List of payments
     */
    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Get payment history for current user")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory() {
        List<PaymentResponse> payments = paymentService.getPaymentHistory();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Get payments for a subscription.
     * 
     * @param subscriptionId Subscription ID
     * @return List of payments
     */
    @GetMapping("/subscription/{subscriptionId}")
    @Operation(summary = "Get payments by subscription", description = "Get all payments for a specific subscription")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsBySubscription(
            @PathVariable String subscriptionId) {
        List<PaymentResponse> payments = paymentService.getPaymentsBySubscription(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Get revenue statistics for admin dashboard.
     * 
     * @return Revenue statistics
     */
    @GetMapping("/admin/revenue-stats")
    @Operation(summary = "Get revenue stats", description = "Get revenue statistics for admin dashboard")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getRevenueStats() {
        java.util.Map<String, Object> stats = paymentService.getRevenueStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
