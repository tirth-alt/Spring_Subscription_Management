package com.example.subscription_platform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service for sending emails.
 * Uses Spring Mail with JavaMailSender and Thymeleaf for HTML templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Send email asynchronously.
     * 
     * @param to           Recipient email
     * @param subject      Email subject
     * @param templateName Thymeleaf template name
     * @param variables    Template variables
     */
    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@subscriptionplatform.com");

            // Process template
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send welcome email to new user.
     */
    public void sendWelcomeEmail(String to, String firstName) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "loginUrl", "http://localhost:3000/login");
        sendEmail(to, "Welcome to Subscription Platform!", "welcome-email", variables);
    }

    /**
     * Send subscription confirmation email.
     */
    public void sendSubscriptionConfirmation(String to, String firstName, String planName, String endDate) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "planName", planName,
                "endDate", endDate,
                "dashboardUrl", "http://localhost:3000/dashboard");
        sendEmail(to, "Subscription Activated - " + planName, "subscription-confirmation", variables);
    }

    /**
     * Send subscription expiry reminder.
     */
    public void sendExpiryReminder(String to, String firstName, String planName, String expiryDate) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "planName", planName,
                "expiryDate", expiryDate,
                "renewUrl", "http://localhost:3000/renew");
        sendEmail(to, "Your Subscription is Expiring Soon", "expiry-reminder", variables);
    }

    /**
     * Send payment receipt email.
     */
    public void sendPaymentReceipt(String to, String firstName, String amount, String planName, String invoiceUrl) {
        Map<String, Object> variables = Map.of(
                "firstName", firstName,
                "amount", amount,
                "planName", planName,
                "invoiceUrl", invoiceUrl,
                "dashboardUrl", "http://localhost:3000/dashboard");
        sendEmail(to, "Payment Receipt - ₹" + amount, "payment-receipt", variables);
    }
}
