package com.pharmacopee.ladafura.configs.security;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtre de limitation de débit (Rate Limiting) appliqué aux endpoints de l'API (/api/v1/**).
 * Par défaut : maximum 5 requêtes toutes les 5 minutes (300 secondes) par utilisateur authentifié
 * ou par adresse IP (pour les visiteurs non connectés) et par endpoint.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, RateLimitWindow> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${rate-limit.window-seconds:300}")
    private long windowSeconds;

    /**
     * Portée du compteur :
     * - ENDPOINT : 5 requêtes / 5 min par (utilisateur ou IP) et par endpoint (méthode + URI)
     * - GLOBAL   : 5 requêtes / 5 min par (utilisateur ou IP) tous endpoints confondus
     */
    @Value("${rate-limit.scope:ENDPOINT}")
    private String scope;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long nowEpochSeconds = Instant.now().getEpochSecond();
        cleanupExpiredBucketsIfNeeded(nowEpochSeconds);

        String bucketKey = resolveBucketKey(request);
        RateLimitWindow window = buckets.compute(bucketKey, (key, existing) -> {
            if (existing == null || nowEpochSeconds >= existing.resetEpochSeconds()) {
                return new RateLimitWindow(new AtomicInteger(1), nowEpochSeconds + windowSeconds);
            }
            existing.counter().incrementAndGet();
            return existing;
        });

        int currentCount = window.counter().get();
        long retryAfterSeconds = Math.max(1L, window.resetEpochSeconds() - nowEpochSeconds);
        int remaining = Math.max(0, maxRequests - currentCount);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(retryAfterSeconds));

        if (currentCount > maxRequests) {
            log.warn("Rate limit dépassé pour [{}] ({}/{} requêtes). Réinitialisation dans {}s",
                    bucketKey, currentCount, maxRequests, retryAfterSeconds);

            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            errorDetails.put("error", "Too Many Requests");
            errorDetails.put("message", String.format(
                    "Limite de requêtes atteinte (maximum %d requêtes toutes les %d minutes). Veuillez réessayer dans %d secondes.",
                    maxRequests, Math.max(1, windowSeconds / 60), retryAfterSeconds));
            errorDetails.put("retryAfterSeconds", retryAfterSeconds);
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getOutputStream(), errorDetails);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBucketKey(HttpServletRequest request) {
        String clientId = resolveClientIdentifier(request);
        if ("GLOBAL".equalsIgnoreCase(scope)) {
            return clientId;
        }
        return clientId + ":" + request.getMethod() + ":" + request.getRequestURI();
    }

    private String resolveClientIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + extractClientIp(request);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void cleanupExpiredBucketsIfNeeded(long nowEpochSeconds) {
        if (buckets.size() > 2000) {
            buckets.entrySet().removeIf(entry -> nowEpochSeconds >= entry.getValue().resetEpochSeconds());
        }
    }

    private record RateLimitWindow(AtomicInteger counter, long resetEpochSeconds) {
    }
}

