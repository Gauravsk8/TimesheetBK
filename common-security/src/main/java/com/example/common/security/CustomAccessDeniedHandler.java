package com.example.common.security;

import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .error_code(errorCode.FORBIDDEN_ERROR)
                .message(errorMessage.UNAUTHORIZED_ACCESS)
                .property("")
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}