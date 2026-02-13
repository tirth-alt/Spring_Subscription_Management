package com.example.subscription_platform.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j.
 * Limits API requests per IP address.
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Rate limit: 100 requests per minute per IP.
     */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get client IP
        String ip = getClientIP(httpRequest);

        // Get or create bucket for this IP
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        // Try to consume 1 token
        if (bucket.tryConsume(1)) {
            // Request allowed
            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {}", ip);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter()
                    .write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
        }
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
