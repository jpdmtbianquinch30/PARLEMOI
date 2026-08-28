package sn.parlemoi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sn.parlemoi.backend.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        PathPatternRequestMatcher.Builder mvc = PathPatternRequestMatcher.withDefaults();

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(mvc.matcher("/error")).permitAll()

                        .requestMatchers(
                                mvc.matcher("/api/auth/login"),
                                mvc.matcher("/api/public/**"),
                                mvc.matcher("/api/reservations/**"),
                                mvc.matcher("/api/conversations/**"),
                                mvc.matcher("/api/creneaux/**"),
                                mvc.matcher("/api/services/**"),
                                mvc.matcher("/ws/**")
                        ).permitAll()

                        // Verification par role reel contenu dans le JWT, pas juste "authenticated"
                        .requestMatchers(mvc.matcher("/api/admin/**")).hasRole("ADMIN")
                        .requestMatchers(mvc.matcher("/api/ecoutant/**")).hasAnyRole("ECOUTANT", "ADMIN")

                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        // Bean vide intentionnel : notre auth est 100% JWT maison (voir JwtAuthenticationFilter).
        // Sa seule presence suffit a desactiver la generation du user in-memory par defaut de Spring Boot.
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                    "Authentification par UserDetailsService non utilisee - voir JwtAuthenticationFilter"
            );
        };
    }
}