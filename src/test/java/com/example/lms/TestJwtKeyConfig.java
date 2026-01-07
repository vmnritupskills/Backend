package com.example.lms;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestJwtKeyConfig {

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return (RSAPrivateKey) kpg.generateKeyPair().getPrivate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return (RSAPublicKey) kpg.generateKeyPair().getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}