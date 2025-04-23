package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.Service.KeycloakAssignRoleService;
import com.example.IdentityManagementService.dto.request.UserRoleAssignRequestDto;
import com.example.common.annotations.RequiresKeycloakAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.IdentityManagementService.Service.KeycloakCreateUserService;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class KeycloakAdminController {

    private final KeycloakCreateUserService keycloakAdminService;
    private final KeycloakAssignRoleService keycloakAssignRoleService;

    //Create User
    @RequiresKeycloakAuthorization(resource = "employee", scope = "testscope")
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestHeader("Authorization") String token,
            @RequestBody EmployeeRequestDto dto
    ) {
        String userId = keycloakAdminService.createUser(dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("keycloakUserId", userId);
        return ResponseEntity.status(201).body(response);
    }


    @PostMapping("/assign-roles")
    @RequiresKeycloakAuthorization(resource = "employee", scope = "testscope")
    public ResponseEntity<String> assignRoles(
            @RequestHeader("Authorization") String token,
            @RequestBody UserRoleAssignRequestDto requestDto
    ) {
        keycloakAssignRoleService.assignRealmRoles(requestDto.getUserId(), requestDto.getRoles());
        return ResponseEntity.ok("Roles assigned successfully");
    }


    @GetMapping("User/{username}")
    public ResponseEntity<Map<String, String>> getUserByUsername(@PathVariable String username) {
        var user = keycloakAdminService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = new HashMap<>();
        response.put("keycloakUserId", user.getId());
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("User/{id}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String id) {
        var user = keycloakAdminService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = new HashMap<>();
        response.put("keycloakUserId", user.getId());
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }

}

