package com.example.timesheet.client;

import com.example.timesheet.config.FeignClientConfig;
import com.example.timesheet.dto.request.EmployeeRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "IdentityServiceClient",
        url = "${identity.service.url}",
        configuration = FeignClientConfig.class
)
public interface IdentityServiceClient {
    @PostMapping("/api/identity/create-user")
    ResponseEntity<Map<String, String>> createKeycloakUser(
            @RequestHeader("Authorization") String token,
            @RequestBody EmployeeRequestDto dto,
            @RequestParam String role
    );
}
