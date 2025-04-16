package com.example.timesheet.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// FeignClientConfig.java
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final FeignErrorDecoder errorDecoder;

    @Bean
    public ErrorDecoder errorDecoder() {
        return errorDecoder;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String token = attributes.getRequest().getHeader("Authorization");
                requestTemplate.header("Authorization", token);
            }
        };
    }
}