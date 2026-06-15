package com.tomato.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomato.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple fixed-window rate limiter for the login and registration endpoints, keyed by
 * client IP, to slow down credential-stuffing and account-enumeration attempts.
 *
 * This is in-memory and per-instance. If the app is ever scaled to multiple
 * instances, replace this with a shared store (e.g. Redis) so limits apply
 * across all instances.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    private final ObjectMapper objectMapper;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            ObjectMapper objectMapper,
            @Value("${tomato.security.rate-limit.auth.max-requests:10}") int maxRequests,
            @Value("${tomato.security.rate-limit.auth.window-seconds:60}") long windowSeconds) {
        this.objectMapper = objectMapper;
        this.maxRequests = maxRequests;
        this.windowMillis = Duration.ofSeconds(windowSeconds).toMillis();

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limit-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        cleaner.scheduleAtFixedRate(this::evictStaleWindows, windowSeconds, windowSeconds, TimeUnit.SECONDS);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        RequestWindow window = windows.computeIfAbsent(key, k -> new RequestWindow());

        if (window.tryConsume(windowMillis, maxRequests)) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (window.windowStart + windowMillis - System.currentTimeMillis()) / 1000);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Too many requests. Please try again later.",
                List.of());
        objectMapper.writeValue(response.getWriter(), body);
    }

    private void evictStaleWindows() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > windowMillis);
    }

    private static final class RequestWindow {
        private volatile long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean tryConsume(long windowMillis, int maxRequests) {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMillis) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }
}
