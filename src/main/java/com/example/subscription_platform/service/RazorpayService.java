package com.example.subscription_platform.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Service for Razorpay payment gateway integration.
 * Handles order creation, payment verification, and webhooks.
 */
@Slf4j
@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayService(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret) throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        log.info("Razorpay client initialized");
    }

    /**
     * Create a Razorpay order.
     * 
     * @param amount   Amount in paise (multiply rupees by 100)
     * @param currency Currency code (INR)
     * @param receipt  Receipt ID
     * @return Razorpay order ID
     */
    public String createOrder(Integer amount, String currency, String receipt) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount); // Amount in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);

            log.info("Razorpay order created: " + order.get("id"));
            return order.get("id");

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order", e);
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    /**
     * Verify payment signature.
     * 
     * @param orderId   Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Payment signature
     * @return true if signature is valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = generateSignature(payload, keySecret);

            boolean isValid = generatedSignature.equals(signature);
            log.info("Payment signature verification: " + (isValid ? "SUCCESS" : "FAILED"));

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying payment signature", e);
            return false;
        }
    }

    /**
     * Verify webhook signature.
     * 
     * @param payload   Webhook payload
     * @param signature Webhook signature from header
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String generatedSignature = generateSignature(payload, webhookSecret);

            boolean isValid = generatedSignature.equals(signature);
            log.info("Webhook signature verification: " + (isValid ? "SUCCESS" : "FAILED"));

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Fetch payment details from Razorpay.
     * 
     * @param paymentId Payment ID
     * @return Payment object
     */
    public JSONObject fetchPayment(String paymentId) {
        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            return payment.toJson();

        } catch (RazorpayException e) {
            log.error("Error fetching payment: {}", paymentId, e);
            throw new RuntimeException("Failed to fetch payment details", e);
        }
    }

    /**
     * Generate HMAC SHA256 signature.
     */
    private String generateSignature(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Convert bytes to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
