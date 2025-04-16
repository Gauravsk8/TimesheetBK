package com.example.IdentityManagementService.config;

import com.example.common.security.CustomAccessDeniedHandler;
import com.example.common.security.CustomEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomEntryPoint customEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(CustomEntryPoint customEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customEntryPoint = customEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Value("${keycloak.enabled:true}")
    private boolean keycloakEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is disabled because this application is stateless and uses JWT tokens in Authorization headers.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/secured").authenticated()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/identity/create-user").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(customEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
                );

        if (keycloakEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .authenticationEntryPoint(customEntryPoint)
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        }

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("ROLE_");
        converter.setAuthoritiesClaimName("realm_access.roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }
}
