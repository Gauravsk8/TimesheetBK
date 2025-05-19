package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.Service.EmployeeService;
import com.example.IdentityManagementService.Service.KeycloakAssignRoleService;
import com.example.IdentityManagementService.Service.KeycloakCreateUserService;
import com.example.IdentityManagementService.Service.ServiceImpl.KeycloakAssignRoleServiceImpl;
import com.example.IdentityManagementService.Service.ServiceImpl.KeycloakCreateUserServiceImpl;
import com.example.IdentityManagementService.dto.request.UserRoleAssignRequestDto;
import com.example.IdentityManagementService.dto.request.UserRoleUpdateRequestDto;
import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.constants.MessageConstants;
import com.example.common.dto.PasswordUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;

import java.util.*;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
public class KeycloakController {

    private final KeycloakCreateUserService keycloakAdminService;
    private final KeycloakAssignRoleService keycloakAssignRoleService;

    //Create User Common for both keycloak and DB

    @PostMapping("/users")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:add")
    public ResponseEntity<Map<String, String>> createUser(
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        Map<String, String> result = keycloakAdminService.createUser(dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        return ResponseEntity.ok().body(response);
    }


    @PatchMapping("/users/my")
    @RequiresKeycloakAuthorization(resource = "idms:user", scope = "idms:user:update")
    public ResponseEntity<String> editOwnProfile(@Valid @RequestBody EmployeeRequestDto dto) {
        String keycloakUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        keycloakAdminService.updateOwnProfile(keycloakUserId, dto);

        return ResponseEntity.ok(MessageConstants.EMPLOYEE_UPDATED_SUCCESSFULLY);
    }





    @PatchMapping("/users/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:update")
    public ResponseEntity<String> editEmployeeProfile(
            @PathVariable String employeeCode,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        keycloakAdminService.updateUserProfile(employeeCode, dto); // Now handles both Keycloak + DB
        return ResponseEntity.ok(MessageConstants.EMPLOYEE_UPDATED_SUCCESSFULLY);
    }


    //update password
    @PostMapping("/users/my/password")
    @RequiresKeycloakAuthorization(resource = "idms:user", scope = "idms:user:update")
    public ResponseEntity<String> updateOwnPassword(@Valid @RequestBody PasswordUpdateRequestDto request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        keycloakAdminService.updateUserPassword(userId, request.getNewPassword());

        return ResponseEntity.ok(MessageConstants.PASSWORD_UPDATED_SUCCESSFULLY);
    }








    //ROLES
    @PostMapping("/users/roles")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:add")
    public ResponseEntity<String> assignRoles(
            @RequestBody UserRoleAssignRequestDto requestDto
    ) {
        keycloakAssignRoleService.assignRealmRoles(requestDto.getEmployeeCode(), requestDto.getRoles());
        return ResponseEntity.ok(MessageConstants.ROLES_ASSIGNED_SUCCESSFULLY);
    }

    @DeleteMapping("/users/roles")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:add")
    public ResponseEntity<String> unassignRoles(
            @RequestBody UserRoleAssignRequestDto requestDto
    ) {
        keycloakAssignRoleService.unassignRealmRoles(requestDto.getEmployeeCode(), requestDto.getRoles());
        return ResponseEntity.ok(MessageConstants.ROLES_UNASSIGNED_SUCCESSFULLY);
    }

    @PutMapping("/users/roles")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:update")
    public ResponseEntity<String> updateUserRoles(
            @RequestBody UserRoleUpdateRequestDto requestDto
    ) {
        keycloakAssignRoleService.updateUserRoles(
                requestDto.getEmployeeCode(),
                requestDto.getRolesToAssign(),
                requestDto.getRolesToRemove()
        );
        return ResponseEntity.ok(MessageConstants.ROLES_UPDATED_SUCCESSFULLY);
    }

    @GetMapping("/users/{employeeCode}/manager_role")
    @RequiresKeycloakAuthorization(resource = "tms:com", scope = "tms:com:get")
    public ResponseEntity<Boolean> hasManagerRole(@PathVariable String employeeCode, @RequestParam String roleName) {
        boolean hasRole = keycloakAssignRoleService.hasManagerRole(employeeCode, roleName);
        return ResponseEntity.ok(hasRole);
    }

    @GetMapping("/users/{employeeCode}/roles")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:get")
    public ResponseEntity<List<String>> getUserAssignedRealmRoles(@PathVariable String employeeCode) {
        List<String> roles = keycloakAssignRoleService.getAssignedRealmRoles(employeeCode);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/users/roles")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:get")
    public ResponseEntity<Map<String, String>> getUsersByRoles(@RequestParam List<String> roles) {
        Map<String, String> users = keycloakAssignRoleService.getUsersByRoles(roles);
        return ResponseEntity.ok(users);
    }


}

