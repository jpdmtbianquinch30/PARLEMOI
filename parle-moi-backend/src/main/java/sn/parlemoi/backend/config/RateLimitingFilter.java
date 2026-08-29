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
import java.util.regex.Pattern;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Route de lecture par code : cible directe d'une attaque par force brute sur le code de conversation.
    // Limite volontairement plus stricte que le reste - on n'a besoin de consulter son propre historique
    // que quelques fois par minute dans un usage legitime.
    private static final Pattern ROUTE_LECTURE_PAR_CODE =
            Pattern.compile("^/api/conversations/[A-Za-z0-9-]+(/historique)?$");

    private final ConcurrentMap<String, Bucket> bucketsGeneraux = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bucket> bucketsLectureCode = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        boolean estLectureGet = "GET".equalsIgnoreCase(request.getMethod());

        if (estLectureGet && ROUTE_LECTURE_PAR_CODE.matcher(uri).matches()) {
            Bucket bucket = bucketsLectureCode.computeIfAbsent(ip, k -> creerBucketLectureCode());
            if (!bucket.tryConsume(1)) {
                repondreTropDeRequetes(response);
                return;
            }
        } else if (estRouteLimitee(uri)) {
            Bucket bucket = bucketsGeneraux.computeIfAbsent(ip, k -> creerBucketGeneral());
            if (!bucket.tryConsume(1)) {
                repondreTropDeRequetes(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean estRouteLimitee(String uri) {
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/reservations")
                || uri.startsWith("/api/conversations")
                || uri.startsWith("/api/paiements");
    }

    private void repondreTropDeRequetes(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"statut\":429,\"erreur\":\"Trop de requetes\",\"message\":\"Veuillez reessayer plus tard\"}"
        );
    }

    private Bucket creerBucketGeneral() {
        Bandwidth limite = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limite).build();
    }

    private Bucket creerBucketLectureCode() {
        // 5 tentatives / minute / IP - suffisant pour un usage legitime (l'utilisateur connait deja son code),
        // beaucoup trop lent pour une attaque par force brute sur 852 milliards de combinaisons possibles
        Bandwidth limite = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limite).build();
    }
}