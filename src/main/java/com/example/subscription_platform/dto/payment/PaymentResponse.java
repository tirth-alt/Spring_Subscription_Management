package com.example.subscription_platform.dto.payment;

import com.example.subscription_platform.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String id;
    private String userId;
    private String subscriptionId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
}
