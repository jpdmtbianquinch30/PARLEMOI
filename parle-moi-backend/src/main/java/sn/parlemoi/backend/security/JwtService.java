package sn.parlemoi.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.enums.RoleEcoutant;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

    private final SecretKey cleSecrete;
    private final long dureeValiditeMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long dureeValiditeMs
    ) {
        this.cleSecrete = Keys.hmacShaKeyFor(secret.getBytes());
        this.dureeValiditeMs = dureeValiditeMs;
    }

    public String genererToken(String ecoutantId, String email, RoleEcoutant role) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + dureeValiditeMs);

        return Jwts.builder()
                .subject(ecoutantId)
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cleSecrete)
                .compact();
    }

    public String extraireEcoutantId(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public String extraireRole(String token) {
        return extraireClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean estValide(String token) {
        try {
            Date expiration = extraireClaim(token, Claims::getExpiration);
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(cleSecrete)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}