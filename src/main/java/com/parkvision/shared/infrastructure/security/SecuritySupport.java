package com.parkvision.shared.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;

/**
 * Soporte de seguridad reutilizable por todos los microservicios (resource server). Centraliza las dos
 * piezas comunes que antes vivían en el {@code SecurityConfig} del monolito: el mapeo del claim
 * {@code role} del JWT a {@code ROLE_<role>} y la configuración CORS. Cada servicio define su propia
 * cadena de filtros (rutas públicas/protegidas) pero reutiliza estas piezas para no duplicarlas.
 */
public final class SecuritySupport {

    private static final Logger log = LoggerFactory.getLogger(SecuritySupport.class);

    private SecuritySupport() {
    }

    /**
     * Construye un {@link JwtDecoder} (resource server) a partir de la clave pública RSA en PEM
     * (X.509/SubjectPublicKeyInfo), normalmente inyectada vía {@code IAM_JWT_PUBLIC_KEY}. La usan los
     * servicios que NO emiten tokens (parking, occupancy, prediction, notifications) para verificar los
     * JWT firmados por {@code iam}. Si el PEM está vacío se genera un par EFÍMERO solo para que el
     * servicio arranque en dev: ningún token real validará hasta configurar la clave pública compartida.
     */
    public static JwtDecoder jwtDecoderFromPublicKey(String publicKeyPem) {
        if (StringUtils.hasText(publicKeyPem)) {
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(toStream(publicKeyPem));
            log.info("JwtDecoder cargado desde iam.jwt.public-key.");
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        }
        log.warn("iam.jwt.public-key NO configurada: se genera una clave pública EFÍMERA. Los JWT emitidos "
                + "por iam NO validarán en este servicio hasta configurar IAM_JWT_PUBLIC_KEY (la misma "
                + "clave pública en todos los servicios).");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            RSAPublicKey ephemeral = (RSAPublicKey) generator.generateKeyPair().getPublic();
            return NimbusJwtDecoder.withPublicKey(ephemeral).build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar la clave RSA efímera", e);
        }
    }

    private static InputStream toStream(String pem) {
        // Permite pasar el PEM en una sola línea con "\n" literales (típico en variables de entorno).
        return new ByteArrayInputStream(pem.replace("\\n", "\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convierte el claim {@code role} del JWT (RS256 emitido por {@code iam}) en la autoridad
     * {@code ROLE_<role>} que entiende Spring Security. Idéntico al converter del monolito.
     */
    public static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    /**
     * Fuente de configuración CORS común. En el despliegue de microservicios CORS se resuelve en el
     * gateway; esto sirve para arranques directos del servicio (dev) o defensa en profundidad.
     *
     * @param allowedOriginPatterns patrones de origen permitidos (p.ej. el frontend Netlify y localhost).
     */
    /**
     * Igual que {@link #corsConfigurationSource(List)} pero recibe los orígenes como una cadena separada
     * por comas — cómodo para inyectarlos desde una variable de entorno ({@code cors.allowed-origins} /
     * {@code CORS_ALLOWED_ORIGINS}) en vez de hardcodearlos.
     */
    public static CorsConfigurationSource corsConfigurationSource(String allowedOriginPatternsCsv) {
        return corsConfigurationSource(Arrays.stream(allowedOriginPatternsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    }

    public static CorsConfigurationSource corsConfigurationSource(List<String> allowedOriginPatterns) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
