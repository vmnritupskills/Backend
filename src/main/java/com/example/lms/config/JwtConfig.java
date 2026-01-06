package com.example.lms.config;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class JwtConfig {

    @Value("${jwt.keystore.path}")
    private Resource keystore;

    @Value("${jwt.keystore.type}")
    private String keystoreType;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.key-alias}")
    private String keyAlias;

    @Value("${jwt.keystore.key-password}")
    private String keyPassword;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        KeyStore keyStore = loadKeyStore();
        Key key = keyStore.getKey(keyAlias, keyPassword.toCharArray());
        return (RSAPrivateKey) key;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        KeyStore keyStore = loadKeyStore();
        Certificate cert = keyStore.getCertificate(keyAlias);
        return (RSAPublicKey) cert.getPublicKey();
    }

    private KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        try (InputStream is = keystore.getInputStream()) {
            keyStore.load(is, keystorePassword.toCharArray());
        }
        return keyStore;
    }
}