package com.parkvision.iam.infrastructure.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
public class IamConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IamConfiguration.class);

    /** Clave pública RSA en formato PEM (X.509/SubjectPublicKeyInfo). Inyectada vía {@code IAM_JWT_PUBLIC_KEY}. */
    @Value("${iam.jwt.public-key:}")
    private String publicKeyPem;

    /** Clave privada RSA en formato PEM (PKCS#8). Inyectada vía {@code IAM_JWT_PRIVATE_KEY}. */
    @Value("${iam.jwt.private-key:}")
    private String privateKeyPem;

    /**
     * Par RSA usado para firmar/verificar los JWT. Si {@code iam.jwt.public-key} y
     * {@code iam.jwt.private-key} están configuradas, se cargan desde ahí (estables entre reinicios
     * y entre instancias). En caso contrario se genera un par EFÍMERO solo para desarrollo local.
     */
    @Bean
    public KeyPair rsaKeyPair() throws NoSuchAlgorithmException {
        if (StringUtils.hasText(publicKeyPem) && StringUtils.hasText(privateKeyPem)) {
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(toStream(publicKeyPem));
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(toStream(privateKeyPem));
            log.info("JWT RSA keys cargadas desde la configuración (iam.jwt.public-key/private-key).");
            return new KeyPair(publicKey, privateKey);
        }
        log.warn("JWT RSA keys NO configuradas (iam.jwt.public-key/iam.jwt.private-key): "
                + "se genera un par EFÍMERO. Los tokens emitidos no sobreviven a un reinicio ni "
                + "funcionan en despliegues multi-instancia. Configura las claves para producción.");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static InputStream toStream(String pem) {
        // Permite pasar el PEM en una sola línea con "\n" literales (típico en variables de entorno).
        return new ByteArrayInputStream(pem.replace("\\n", "\n").getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(KeyPair rsaKeyPair) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) rsaKeyPair.getPublic())
                .privateKey(rsaKeyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
