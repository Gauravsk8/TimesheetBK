package com.example.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MDCLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            MDC.put("x-trace-id", getOrGenerate(request, "x-trace-id"));
            MDC.put("x-client-id", request.getHeader("x-client-id"));
            MDC.put("x-tenant-id", request.getHeader("x-tenant-id"));
            MDC.put("x-business-group-id", request.getHeader("x-business-group-id"));

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // Prevent memory leaks
        }
    }

    private String getOrGenerate(HttpServletRequest request, String header) {
        String value = request.getHeader(header);

        return value != null && !value.isEmpty() ? value : java.util.UUID.randomUUID().toString();
    }
}

