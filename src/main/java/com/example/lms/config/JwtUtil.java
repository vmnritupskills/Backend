package com.example.lms.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.lms.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    @Value("${jwt.access-token.expiry}")
    private long accessTokenExpiry;
    @Value("${jwt.refresh-token.expiry}")
    private long refreshTokenExpiry;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtUtil(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().setSubject(user.getEmail()).claim("userId", user.getId()).claim("role", user.getRole().getName())
                .setId(UUID.randomUUID().toString()).setIssuedAt(Date.from(now)) // 1 hour
                .setExpiration(Date.from(now.plusSeconds(accessTokenExpiry))).signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();

        return Jwts.builder().setSubject(subject).setId(UUID.randomUUID().toString()).setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTokenExpiry))) // 7 days
                .signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
    }
}