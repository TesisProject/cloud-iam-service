package com.parkvision.iam.infrastructure.config;

import com.parkvision.shared.infrastructure.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Seguridad del microservicio IAM. Cadena stateless con verificación de JWT (RS256) vía resource server.
 * Rutas públicas: registro de usuario, recuperación/login (auth) y Swagger. Las rutas {@code /internal/**}
 * son consumidas servicio-a-servicio (introspección de API-key, reseñas de zona) y quedan abiertas a
 * nivel de servicio porque el gateway bloquea su acceso externo. El resto exige JWT; {@code api-keys/**}
 * requiere rol ADMIN. El {@code JwtDecoder}/{@code JwtEncoder} los aporta {@link IamConfiguration}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Orígenes CORS permitidos (separados por comas). Configurable vía la env var CORS_ALLOWED_ORIGINS.
    // Default: frontend Netlify, localhost y el dominio de Azure Container Apps (Swagger vía gateway).
    @Value("${cors.allowed-origins:https://park-vision-frontend.netlify.app,http://localhost:*,https://*.azurecontainerapps.io}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(
                        SecuritySupport.corsConfigurationSource(allowedOrigins)))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/error").permitAll()
                        // Endpoints internos (servicio-a-servicio); el gateway los bloquea desde fuera
                        .requestMatchers("/api/v1/iam/internal/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/iam/users").permitAll()
                        .requestMatchers("/api/v1/iam/auth/**").permitAll()
                        .requestMatchers("/api/v1/iam/api-keys/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                SecuritySupport.jwtAuthenticationConverter())));
        return http.build();
    }
}
