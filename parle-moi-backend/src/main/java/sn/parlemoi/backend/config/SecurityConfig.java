package sn.parlemoi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Spring Security 7 : builder officiel recommande pour le matching d'URL,
        // remplace les anciens AntPathRequestMatcher/MvcRequestMatcher desormais supprimes
        PathPatternRequestMatcher.Builder mvc = PathPatternRequestMatcher.withDefaults();

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // Route technique - necessaire pour laisser Spring Boot afficher ses pages d'erreur standard (404, 500...)
                        .requestMatchers(mvc.matcher("/error")).permitAll()

                        // Routes publiques - catalogue, reservation, recherche par code, websocket handshake
                        .requestMatchers(
                                mvc.matcher("/api/public/**"),
                                mvc.matcher("/api/reservations/**"),
                                mvc.matcher("/api/creneaux/**"),
                                mvc.matcher("/ws/**")
                        ).permitAll()

                        // Routes admin - protegees, authentification requise (JWT a l'etape suivante)
                        .requestMatchers(mvc.matcher("/api/admin/**")).authenticated()

                        // Tout le reste - bloque par defaut (principe du moindre privilege)
                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}