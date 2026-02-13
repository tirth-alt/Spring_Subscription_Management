package com.example.subscription_platform.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing a payment transaction.
 * Stores Razorpay payment details and links to subscriptions.
 * Indexed on userId, subscriptionId, and razorpayOrderId for efficient queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    @Indexed
    private String userId;

    @NotBlank(message = "Subscription ID is required")
    @Indexed
    private String subscriptionId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Payment status is required")
    @Indexed
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethod; // card, upi, netbanking, etc.

    // Razorpay specific fields
    @Indexed
    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    // Additional metadata
    private String errorMessage; // Store error if payment fails

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Helper method to check if payment is successful
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
}
