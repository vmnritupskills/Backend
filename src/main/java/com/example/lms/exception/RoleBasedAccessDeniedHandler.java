package com.example.lms.exception;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class RoleBasedAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        String message = "Access denied";

        // Customize message based on endpoint
        String path = request.getRequestURI();

        if (path.startsWith("/api/admin")) {
            message = "Access denied. This API is only for ADMIN users";
        } else if (path.startsWith("/api/institution")) {
            message = "Access denied. This API is only for INSTITUTION users";
        } else if (path.startsWith("/api/content-manager")) {
            message = "Access denied. This API is only for CONTENT MANAGER users";
        } else if(path.startsWith("/api/student")) {
            message = "Access denied. This API is only for STUDENT users";
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write("""
            {
              "status": 403,
              "message": "%s"
            }
        """.formatted(message));
    }
}
