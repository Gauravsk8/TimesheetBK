package com.example.IdentityManagementService.security;

import com.example.IdentityManagementService.constants.errorCode;
import com.example.IdentityManagementService.constants.errorMessage;
import com.example.IdentityManagementService.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        final String authHeader = request.getHeader("Authorization");
        String message;

        if (authHeader == null || authHeader.isBlank()) {
            message = errorMessage.MISSING_BEARER_TOKEN;
        } else if (!authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            message = errorMessage.MALFORMED_BEARER_TOKEN; // <-- Add this constant
        } else {
            message = errorMessage.MALFORMED_BEARER_TOKEN;
        }

        ErrorResponse error = ErrorResponse.builder()
                .error_code(errorCode.UNAUTHORIZED_ERROR)
                .message(message)
                .property("")
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
