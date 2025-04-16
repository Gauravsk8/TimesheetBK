package com.example.IdentityManagementService.Service;

import com.example.common.constants.errorCode;
import com.example.IdentityManagementService.exceptions.KeycloakException;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.common.constants.errorCode.KEYCLOAK_USER_CREATION_FAILED;
import static com.example.common.constants.errorCode.ROLE_ASSIGNMENT_FAILED;
import static com.example.common.constants.errorMessage.*;


@Service
@RequiredArgsConstructor
public class KeycloakService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);
    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    public String createUserWithRole(EmployeeRequestDto employee, String roleName) {
        Response response = null;
        try {
            // 1. Verify admin connection first
            verifyAdminConnection();

            RealmResource realmResource = keycloakAdmin.realm(realm);

            // 2. Check if role exists BEFORE creating user
            RoleRepresentation role = verifyRoleExists(roleName, realmResource);

            // 3. Check for existing user
            checkExistingUser(employee.getEmail(), realmResource);

            // 4. Create user representation
            UserRepresentation user = createUserRepresentation(employee);

            // 5. Create user in Keycloak
            UsersResource usersResource = realmResource.users();
            response = usersResource.create(user);
            handleCreateUserResponse(response);

            // 6. Get created user ID
            String userId = extractUserIdFromResponse(response);

            try {
                // 7. Assign role
                assignRealmRole(userId, roleName, realmResource);
                return userId;
            } catch (Exception e) {
                // Rollback user creation if role assignment fails
                log.error("Role assignment failed, deleting user {}", userId);
                usersResource.get(userId).remove();
                throw new KeycloakException(
                        ROLE_ASSIGNMENT_FAILED,
                        "User created but role assignment failed. Rolled back user creation.",
                        e
                );
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private RoleRepresentation verifyRoleExists(String roleName, RealmResource realmResource) {
        try {
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            if (role == null) {
                throw new KeycloakException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format("Role %s does not exist in Keycloak", roleName)
                );
            }
            return role;
        } catch (Exception e) {
            throw new KeycloakException(
                    errorCode.NOT_FOUND_ERROR,
                    String.format("Failed to verify role %s: %s", roleName, e.getMessage()),
                    e
            );
        }
    }

    private void verifyAdminConnection() {
        try {
            keycloakAdmin.realms().findAll();
        } catch (Exception e) {
            throw new KeycloakException(
                    errorCode.KEYCLOAK_CONNECTION_ERROR,
                    KEYCLOAK_ADMIN_CONNECTION_FAILED,
                    e
            );
        }
    }

    private void checkExistingUser(String email, RealmResource realmResource) {
        List<UserRepresentation> existingUsers = realmResource.users().search(email, true);
        if (!existingUsers.isEmpty()) {
            throw new KeycloakException(
                    errorCode.CONFLICT_ERROR,
                    String.format(KEYCLOAK_USER_ALREADY_EXISTS, email)
            );
        }
    }

    private UserRepresentation createUserRepresentation(EmployeeRequestDto employee) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(employee.getEmail());
        user.setFirstName(employee.getFirstName());
        user.setLastName(employee.getLastName());
        user.setEmail(employee.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(false);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("source", List.of("timesheet-app"));
        user.setAttributes(attributes);

        // Set password securely
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(employee.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        return user;
    }

    private void handleCreateUserResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            String errorBody = null;
            try {
                errorBody = response.readEntity(String.class);
                JsonNode errorNode = new ObjectMapper().readTree(errorBody);
                String errorDetail = errorNode.path("error_description").asText(null);

                if (errorDetail != null) {
                    throw new KeycloakException(
                            KEYCLOAK_USER_CREATION_FAILED,
                            KEYCLOAK_USER_CREATION_FAILED + ": " + errorDetail
                    );
                }
                throw new KeycloakException(
                        KEYCLOAK_USER_CREATION_FAILED,
                        KEYCLOAK_USER_CREATION_FAILED + ": " + errorBody
                );
            } catch (IOException e) {
                throw new KeycloakException(
                        KEYCLOAK_USER_CREATION_FAILED,
                        KEYCLOAK_USER_CREATION_FAILED + ": Unable to parse error response",
                        e
                );
            }
        }
    }

    private String extractUserIdFromResponse(Response response) {
        try {
            String location = response.getLocation().toString();
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new KeycloakException(
                    errorCode.KEYCLOAK_RESPONSE_PARSING_ERROR,
                    "Failed to extract user ID from response",
                    e
            );
        }
    }

    private void assignRealmRole(String userId, String roleName, RealmResource realmResource) {
        try {
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));
        } catch (Exception e) {
            throw new KeycloakException(
                    ROLE_ASSIGNMENT_FAILED,
                    ROLE_ASSIGNMENT_FAILED + ": " + e.getMessage(),
                    e
            );
        }
    }


    // Additional helper methods can be added here
    public boolean isUserExists(String email) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(email, true);
            return !users.isEmpty();
        } catch (Exception e) {
            throw new KeycloakException(errorCode.KEYCLOAK_CONNECTION_ERROR, USER_LOOKUP_FAILED + ": " + e.getMessage(), e);

        }
    }

    public void updateUserPassword(String userId, String newPassword) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            userResource.resetPassword(credential);
        } catch (Exception e) {
            log.error("Error updating user password", e);
            throw new KeycloakException(errorCode.KEYCLOAK_CONNECTION_ERROR, PASSWORD_UPDATE_FAILED + ": " + e.getMessage(), e);
        }
    }
}