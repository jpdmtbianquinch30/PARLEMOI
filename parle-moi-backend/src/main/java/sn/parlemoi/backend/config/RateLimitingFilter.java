package sn.parlemoi.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (estRouteLimitee(request)) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, k -> creerBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"statut\":429,\"erreur\":\"Trop de requetes\",\"message\":\"Veuillez reessayer plus tard\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean estRouteLimitee(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/reservations")
                || uri.startsWith("/api/conversations");
    }

    private Bucket creerBucket() {
        // 10 requetes / minute / IP sur les routes sensibles - a affiner par route en phase 2
        Bandwidth limite = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limite).build();
    }
}