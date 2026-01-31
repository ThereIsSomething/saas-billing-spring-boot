package com.project.saasbilling.config;

import com.project.saasbilling.exception.RateLimitException;
import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${rate-limit.capacity:100}")
    private int capacity;

    @Value("${rate-limit.refill-tokens:100}")
    private int refillTokens;

    @Value("${rate-limit.refill-minutes:1}")
    private int refillMinutes;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientIdentifier(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        if (bucket.tryConsume(1)) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        }

        throw new RateLimitException("Rate limit exceeded. Please try again later.");
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if (userId != null)
            return "user:" + userId;
        String ip = request.getHeader("X-Forwarded-For");
        return "ip:" + (ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr());
    }

    private Bucket createBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofMinutes(refillMinutes))
                        .build())
                .build();
    }
}
