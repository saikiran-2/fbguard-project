package com.fbguard.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory token-bucket rate limiter (Bucket4j) on app submissions.
 * Each user gets their own bucket: 5 submissions per minute, refilling
 * gradually. Prevents one user (or a script) from hammering the verification
 * pipeline - some of those checks hit external APIs, so unrestricted spam is
 * both a cost and an availability risk.
 *
 * NOT a @Component on purpose: it's registered explicitly in SecurityConfig
 * with addFilterAfter(..., JwtAuthFilter.class) so it runs AFTER the JWT
 * filter sets request.getUserPrincipal() - letting a plain @Component
 * auto-register would run it too early in the chain, before auth is known.
 *
 * In-memory means this resets on restart and doesn't share state across
 * multiple backend instances - fine for a single-instance deployment. For
 * multi-instance production, Bucket4j has a Redis-backed ProxyManager that's
 * a drop-in replacement for the ConcurrentHashMap below.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        boolean isAppSubmission = "POST".equalsIgnoreCase(request.getMethod())
                && "/api/apps".equals(request.getRequestURI());

        if (!isAppSubmission) {
            filterChain.doFilter(request, response);
            return;
        }

        // Key by authenticated user if we have one, else by IP (covers pre-auth abuse too).
        String key = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName()
                : request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Too many app submissions - please wait a minute and try again.\"}");
        }
    }
}
