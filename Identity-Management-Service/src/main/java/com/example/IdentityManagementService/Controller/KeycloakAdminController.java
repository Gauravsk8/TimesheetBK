package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.annotations.RequiresKeycloakAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.IdentityManagementService.Service.KeycloakService;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class KeycloakAdminController {

    private final KeycloakService keycloakAdminService;


    @RequiresKeycloakAuthorization(resource = "employee", scope = "testscope")
    // KeycloakAdminController.java
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestHeader("Authorization") String token,
            @RequestBody EmployeeRequestDto dto,
            @RequestParam String role
    ) {
        String userId = keycloakAdminService.createUserWithRole(dto, role);
        return ResponseEntity.ok(Collections.singletonMap("keycloakUserId", userId));
    }
}

