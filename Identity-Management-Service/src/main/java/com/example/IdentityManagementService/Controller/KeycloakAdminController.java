package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.Service.KeycloakAssignRoleService;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.IdentityManagementService.dto.request.UserRoleAssignRequestDto;
import com.example.IdentityManagementService.dto.request.UserRoleUpdateRequestDto;
import com.example.common.annotations.RequiresKeycloakAuthorization;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.IdentityManagementService.Service.KeycloakCreateUserService;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class KeycloakAdminController {

    private final KeycloakCreateUserService keycloakAdminService;
    private final KeycloakAssignRoleService keycloakAssignRoleService;

    //Create User

    @PostMapping("/admin/create-user")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        Map<String, String> result = keycloakAdminService.createUser(dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("keycloakUserId", result.get("userId"));

        return ResponseEntity.status(201).body(response);
    }


    @PatchMapping("/User/my/edit-profile")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public ResponseEntity<String> editOwnProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            var user = keycloakAdminService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            keycloakAdminService.updateUserProfile(user.getId(), dto);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile: " + e.getMessage());
        }
    }


    @PatchMapping("/admin/edit-profile/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> editEmployeeProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String employeeCode,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        var user = keycloakAdminService.getUserByemployeeCode(employeeCode);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        keycloakAdminService.updateUserProfile(user.getId(), dto);
        return ResponseEntity.ok("Employee profile updated successfully");
    }



    @PostMapping("/admin/assign-roles")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> assignRoles(
            @RequestHeader("Authorization") String token,
            @RequestBody UserRoleAssignRequestDto requestDto
    ) {
        keycloakAssignRoleService.assignRealmRoles(requestDto.getEmployeeCode(), requestDto.getRoles());
        return ResponseEntity.ok("Roles assigned successfully");
    }

    @PostMapping("/admin/update-roles")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> updateUserRoles(
            @RequestHeader("Authorization") String token,
            @RequestBody UserRoleUpdateRequestDto requestDto
    ) {
        keycloakAssignRoleService.updateUserRoles(
                requestDto.getEmployeeCode(),
                requestDto.getRolesToAssign(),
                requestDto.getRolesToRemove()
        );
        return ResponseEntity.ok("Roles updated successfully");
    }


    @GetMapping("/admin/User/{employeeCode}/has-project-manager-role")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<Boolean> hasProjectManagerRole(@PathVariable String employeeCode) {
        boolean hasRole = keycloakAssignRoleService.hasProjectManagerRole(employeeCode);
        return ResponseEntity.ok(hasRole);
    }


    @GetMapping("/admin/User/employeeCode/{employeeCode}")
    public ResponseEntity<UserIdentityDto> getUserByemployeeCode(@PathVariable String employeeCode) {
        var user = keycloakAdminService.getUserByemployeeCode(employeeCode);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserIdentityDto dto = new UserIdentityDto();
        dto.setKeycloakUserId(user.getId());
        dto.setEmployeeCode(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/admin/User/Id/{id}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String id) {
        var user = keycloakAdminService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> response = new HashMap<>();
        response.put("keycloakUserId", user.getId());
        response.put("employeeCode", user.getUsername());
        response.put("firstname", user.getFirstName());
        response.put("lastname", user.getLastName());
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, String>>> getAllUsers(@RequestHeader("Authorization") String token) {
        List<UserRepresentation> users = keycloakAdminService.getAllUsers();
        List<Map<String, String>> response = new ArrayList<>();

        for (UserRepresentation user : users) {
            Map<String, String> userMap = new HashMap<>();
            userMap.put("keycloakUserId", user.getId());
            userMap.put("employeeCode", user.getUsername());
            userMap.put("firstname", user.getFirstName());
            userMap.put("lastname", user.getLastName());
            userMap.put("email", user.getEmail());
            response.add(userMap);
        }

        return ResponseEntity.ok(response);
    }
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    @GetMapping("/admin/User/{employeeCode}/assigned-roles")
    public ResponseEntity<List<String>> getUserAssignedRealmRoles(@PathVariable String employeeCode) {
        List<String> roles = keycloakAssignRoleService.getAssignedRealmRoles(employeeCode);
        return ResponseEntity.ok(roles);
    }


    @GetMapping("/admin/Users/by-roles")
    @RequiresKeycloakAuthorization(resource = "Admin",scope =  "Adminscope")
    public ResponseEntity<List<String>> getUsersByRoles(@RequestParam List<String> roles) {
        List<String> users = keycloakAssignRoleService.getUsersByRoles(roles);
        return ResponseEntity.ok(users);
    }

}

