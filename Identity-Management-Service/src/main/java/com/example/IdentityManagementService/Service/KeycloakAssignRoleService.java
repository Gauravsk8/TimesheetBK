package com.example.IdentityManagementService.Service;
import com.example.common.audit.AuditEvent;
import com.example.common.audit.AuditKafkaProducer;

import com.example.IdentityManagementService.exceptions.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.common.constants.errorCode.*;
import static com.example.common.constants.errorCode.INTERNAL_SERVER_ERROR;
import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAssignRoleService {

    private final Keycloak keycloakAdmin;
    private final AuditKafkaProducer auditKafkaProducer;


    @Value("${keycloak.realm}")
    private String realm;

    public void assignRealmRoles(String employeeCode, List<String> roles) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        // Search for user by employeeCode
        List<UserRepresentation> users = realmResource.users().search(employeeCode);
        if (users.isEmpty()) {
            throw new KeycloakException(NOT_FOUND_ERROR, USER_NOT_FOUND + " " + employeeCode);
        }

        // Assuming employeeCode is unique, use the first user found
        UserRepresentation userRepresentation = users.get(0);
        String userId = userRepresentation.getId();
        UserResource userResource = realmResource.users().get(userId);


        // Get currently assigned roles
        List<String> alreadyAssignedRoles = userResource.roles().realmLevel().listEffective().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());

        List<RoleRepresentation> roleRepresentations = new ArrayList<>();
        for (String roleName : roles) {
            if (alreadyAssignedRoles.contains(roleName)) {
                throw new KeycloakException(CONFLICT_ERROR, ROLE_ALREADY_ASSIGNED + roleName);
            }

            try {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                roleRepresentations.add(role);
            } catch (Exception e) {
                throw new KeycloakException(NOT_FOUND_ERROR, ROLE_NOT_FOUND + roleName);
            }
        }

        // Get the UserResource and assign roles
        try {
            userResource.roles().realmLevel().add(roleRepresentations);
            log.info("Assigned roles {} to user {}", roles, employeeCode);
        } catch (Exception e) {
            throw new KeycloakException(NOT_FOUND_ERROR, ROLE_ASSIGNMENT_FAILED + employeeCode);
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String actor = authentication != null ? authentication.getName() : "unknown";

            Map<String, Object> details = new HashMap<>();
            details.put("assignedRoles", roles);
            details.put("userId", userId);
            details.put("employeeCode", employeeCode);
            details.put("assignedBy", actor);

            AuditEvent event = new AuditEvent(
                    "Role-Assign-service",
                    actor,
                    "AssignRealmRoles",
                    Instant.now(),
                    details
            );
            auditKafkaProducer.sendAudit(event);

        } catch (Exception ex) {
            log.error("Audit Kafka send failed", ex);
        }
    }

    public List<String> getAssignedRealmRoles(String employeeCode) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        // Find user by employeeCode
        List<UserRepresentation> users = realmResource.users().search(employeeCode, true);
        if (users.isEmpty()) {
            throw new KeycloakException(
                    NOT_FOUND_ERROR,
                    USER_NOT_FOUND + employeeCode
            );
        }

        String userId = users.get(0).getId();

        // Get realm-level roles assigned to the user
        List<RoleRepresentation> realmRoles = realmResource.users()
                .get(userId)
                .roles()
                .realmLevel()
                .listEffective();

        // Filter out unwanted roles
        return realmRoles.stream()
                .map(RoleRepresentation::getName)
                .filter(role -> !role.equalsIgnoreCase("offline_access"))
                .filter(role -> !role.equalsIgnoreCase("uma_authorization"))
                .filter(role -> !role.equalsIgnoreCase("default-roles-" + realm))
                .collect(Collectors.toList());
    }

    public List<String> getUsersByRoles(List<String> roleNames) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> allUsers = realmResource.users().list();
            List<String> matchedUsers = new ArrayList<>();

            for (UserRepresentation user : allUsers) {
                List<String> userRoleNames = realmResource
                        .users()
                        .get(user.getId())
                        .roles()
                        .realmLevel()
                        .listEffective()
                        .stream()
                        .map(RoleRepresentation::getName)
                        .collect(Collectors.toList());

                boolean hasAllRoles = roleNames.stream()
                        .allMatch(reqRole -> userRoleNames.contains(reqRole));

                if (hasAllRoles) {
                    matchedUsers.add(user.getUsername());
                }
            }

            if (matchedUsers.isEmpty()) {
                throw new KeycloakException(NOT_FOUND_ERROR, USER_NOT_FOUND +" With role " + roleNames);
            }

            return matchedUsers;

        } catch (KeycloakException ex) {
            throw ex; // propagate known business exception
        } catch (Exception ex) {
            log.error("Error while fetching users by roles", ex);
            throw new KeycloakException(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }
    }

    public void updateUserRoles(String employeeCode, List<String> rolesToAssign, List<String> rolesToRemove) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        List<UserRepresentation> users = realmResource.users().search(employeeCode);
        if (users.isEmpty()) {
            throw new KeycloakException(NOT_FOUND_ERROR, USER_NOT_FOUND + " " + employeeCode);
        }

        UserRepresentation userRepresentation = users.get(0);
        String userId = userRepresentation.getId();
        UserResource userResource = realmResource.users().get(userId);

        try {
            if (rolesToAssign != null && !rolesToAssign.isEmpty()) {
                List<RoleRepresentation> assignRoles = new ArrayList<>();
                for (String roleName : rolesToAssign) {

                    RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                    assignRoles.add(role);
                }
                userResource.roles().realmLevel().add(assignRoles);
                log.info("Assigned roles {} to user {}", rolesToAssign, employeeCode);
            }

            if (rolesToRemove != null && !rolesToRemove.isEmpty()) {
                List<RoleRepresentation> removeRoles = new ArrayList<>();
                for (String roleName : rolesToRemove) {
                    RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                    removeRoles.add(role);
                }
                userResource.roles().realmLevel().remove(removeRoles);
                log.info("Removed roles {} from user {}", rolesToRemove, employeeCode);
            }

        } catch (Exception e) {
            throw new KeycloakException(NOT_FOUND_ERROR, "Role update failed for user " + employeeCode);
        }

        //send Audit Kafka
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String actor = authentication != null ? authentication.getName() : "unknown";

            Map<String, Object> details = new HashMap<>();
            details.put("assignedRoles", rolesToAssign);
            details.put("removedRoles", rolesToRemove);
            details.put("userId", userId);
            details.put("employeeCode", employeeCode);
            details.put("updatedBy", actor);

            AuditEvent event = new AuditEvent(
                    "Role-Update-service",
                    actor,
                    "UpdateUserRoles",
                    Instant.now(),
                    details
            );
            auditKafkaProducer.sendAudit(event);

        } catch (Exception ex) {
            log.error("Audit Kafka send failed", ex);
        }
    }


    public boolean hasManagerRole(String employeeCode, String role) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        List<UserRepresentation> users = realmResource.users().search(employeeCode);
        if (users.isEmpty()) {
            throw new KeycloakException(NOT_FOUND_ERROR, USER_NOT_FOUND + " " + employeeCode);
        }

        String userId = users.get(0).getId();

        List<String> assignedRoles = realmResource.users()
                .get(userId)
                .roles()
                .realmLevel()
                .listEffective()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());

        return assignedRoles.contains(role);
    }



}
