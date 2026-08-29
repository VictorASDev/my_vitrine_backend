package com.myvitrine.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Fornece o par de chaves RSA usado para assinar (JwtEncoder) e validar
 * (JwtDecoder) os JWTs. Se {@code myvitrine.security.jwt.private-key} e
 * {@code public-key} nao forem configurados (PEM, com ou sem os cabecalhos
 * -----BEGIN/END-----), um par RSA-2048 efemero e gerado em memoria — valido
 * apenas para desenvolvimento local, ja que qualquer reinicio da aplicacao
 * invalida todos os tokens emitidos anteriormente.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyConfig.class);
    private static final String RSA_ALGORITHM = "RSA";

    @Bean
    public KeyPair jwtKeyPair(JwtProperties jwtProperties) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (StringUtils.hasText(jwtProperties.privateKey()) && StringUtils.hasText(jwtProperties.publicKey())) {
            RSAPrivateKey privateKey = parsePrivateKey(jwtProperties.privateKey());
            RSAPublicKey publicKey = parsePublicKey(jwtProperties.publicKey());
            return new KeyPair(publicKey, privateKey);
        }
        log.warn("Chaves RSA para JWT (myvitrine.security.jwt.private-key/public-key) nao configuradas. "
                + "Gerando um par RSA-2048 efemero para desenvolvimento — todos os tokens emitidos serao "
                + "invalidados no proximo restart da aplicacao. Configure chaves fixas em producao.");
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    public RSAPublicKey jwtPublicKey(KeyPair jwtKeyPair) {
        return (RSAPublicKey) jwtKeyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(KeyPair jwtKeyPair) {
        return (RSAPrivateKey) jwtKeyPair.getPrivate();
    }

    private RSAPublicKey parsePublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(stripPemHeaders(pem)));
        return (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(spec);
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(stripPemHeaders(pem)));
        return (RSAPrivateKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(spec);
    }

    private String stripPemHeaders(String pem) {
        return pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
    }
}
