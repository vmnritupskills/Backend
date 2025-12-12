package com.example.login.config;

import com.example.login.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // Read JWT claims
                Claims claims = jwtUtil.parseToken(token);

                // subject = userId
                String userId = claims.getSubject();

                // extract role (ADMIN / INSTITUTION / STUDENT / CONTENT_MANAGER)
                String role = claims.get("role", String.class);

                // Convert to Spring Security authority
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + role);

                // Create auth object
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(authority) // USER ROLES
                        );

                // Set authentication into context
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ignored) {
                // Token failed → do nothing, user stays unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
