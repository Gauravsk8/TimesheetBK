package com.example.timesheet.config;

import com.example.timesheet.security.CustomAccessDeniedHandler;
import com.example.timesheet.security.CustomEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class SecurityConfig {
    private final CustomEntryPoint customEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(CustomEntryPoint customEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customEntryPoint = customEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    // Define JwtDecoder Bean
    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "http://localhost:8080/realms/timesheet";  // Update with your Keycloak issuer URL
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/employees/create").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(customEntryPoint)
                        .jwt(Customizer.withDefaults()) // The default decoder is now injected
                );

        return http.build();
    }
}
