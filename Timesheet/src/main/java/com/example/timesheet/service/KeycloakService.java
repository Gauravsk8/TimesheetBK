package com.example.timesheet.service;

import com.example.timesheet.constants.errorCode;
import com.example.timesheet.exceptions.TimeSheetException;
import com.example.timesheet.models.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.timesheet.constants.errorMessage.*;


@Service
@RequiredArgsConstructor
public class KeycloakService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);
    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    public String createUserWithRole(Employee employee, String roleName) {
        Response response = null;
        try {
            // 1. Verify admin connection
            verifyAdminConnection();

            RealmResource realmResource = keycloakAdmin.realm(realm);

            // 2. Check for existing user
            checkExistingUser(employee.getEmail(), realmResource);

            // 3. Create user representation with updated credential format
            UserRepresentation user = createUserRepresentation(employee);

            // 4. Create user in Keycloak with enhanced error handling
            UsersResource usersResource = realmResource.users();
            response = usersResource.create(user);
            handleCreateUserResponse(response);

            // 5. Get created user ID
            String userId = extractUserIdFromResponse(response);

            // 6. Assign role
            assignRealmRole(userId, roleName, realmResource);

            return userId;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void verifyAdminConnection() {
        try {
            keycloakAdmin.realms().findAll();
        } catch (Exception e) {
            throw new TimeSheetException(errorCode.KEYCLOAK_CONNECTION_ERROR, KEYCLOAK_ADMIN_CONNECTION_FAILED, e);

        }
    }

    private void checkExistingUser(String email, RealmResource realmResource) {
        List<UserRepresentation> existingUsers = realmResource.users()
                .search(email, true); // exact match
        if (!existingUsers.isEmpty()) {
            throw new TimeSheetException(
                    errorCode.CONFLICT_ERROR,
                    String.format(KEYCLOAK_USER_ALREADY_EXISTS, email)
            );

        }
    }

    private UserRepresentation createUserRepresentation(Employee employee) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(employee.getEmail());
        user.setFirstName(employee.getFirstName());
        user.setLastName(employee.getLastName());
        user.setEmail(employee.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Add any required attributes
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("source", List.of("timesheet-app"));
        user.setAttributes(attributes);

        // Use the newer credential format to avoid deprecation warnings
        user.singleAttribute(CredentialRepresentation.PASSWORD, employee.getPassword());

        return user;
    }

    private void handleCreateUserResponse(Response response) {
        int status = response.getStatus();

        if (status != Response.Status.CREATED.getStatusCode()) {
            String errorBody = null;

            try {
                errorBody = response.readEntity(String.class);

                log.error("Keycloak user creation failed. Status: {}, Headers: {}, Body: {}",
                        status,
                        response.getHeaders(),
                        errorBody);

                // Try to parse error details from JSON response (if available)
                try {
                    JsonNode errorNode = new ObjectMapper().readTree(errorBody);
                    String errorDetail = errorNode.path("error_description").asText(null);

                    if (errorDetail != null && !errorDetail.isEmpty()) {
                        throw new TimeSheetException(errorCode.KEYCLOAK_USER_CREATION_FAILED,
                                KEYCLOAK_USER_CREATION_FAILED + ": " + errorDetail);
                    } else {
                        throw new TimeSheetException(errorCode.KEYCLOAK_USER_CREATION_FAILED,
                                KEYCLOAK_USER_CREATION_FAILED + ": " + errorBody);
                    }

                } catch (IOException parseException) {
                    // Parsing failed, fallback to raw body
                    throw new TimeSheetException(errorCode.KEYCLOAK_USER_CREATION_FAILED,
                            KEYCLOAK_USER_CREATION_FAILED + ": Unable to parse error body: " + errorBody, parseException);
                }

            } catch (Exception e) {
                // Catch any outer-level issue like response reading failure
                throw new TimeSheetException(errorCode.KEYCLOAK_USER_CREATION_FAILED,
                        KEYCLOAK_USER_CREATION_FAILED + ": Unexpected error while reading Keycloak response", e);
            }
        }
    }




    private String extractUserIdFromResponse(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignRealmRole(String userId, String roleName, RealmResource realmResource) {
        try {
            // Get role from realm (not client)
            RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();

            // Assign realm role to user
            realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));
        } catch (Exception e) {
            log.error("Error assigning realm role to user", e);
            throw new TimeSheetException(errorCode.ROLE_ASSIGNMENT_FAILED, ROLE_ASSIGNMENT_FAILED + ": " + e.getMessage(), e);
        }
    }


    // Additional helper methods can be added here
    public boolean isUserExists(String email) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(email, true);
            return !users.isEmpty();
        } catch (Exception e) {
            throw new TimeSheetException(errorCode.KEYCLOAK_CONNECTION_ERROR, USER_LOOKUP_FAILED + ": " + e.getMessage(), e);

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
            throw new TimeSheetException(errorCode.KEYCLOAK_CONNECTION_ERROR, PASSWORD_UPDATE_FAILED + ": " + e.getMessage(), e);
        }
    }
}