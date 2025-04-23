package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;
import com.example.IdentityManagementService.exceptions.KeycloakException;
import com.example.common.constants.errorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.common.constants.errorCode.KEYCLOAK_USER_CREATION_FAILED;
import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakCreateUserService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(EmployeeRequestDto employee) {
        try {
            //To check Admin connection
            verifyAdminConnection();

            //To get into timesheet realm
            RealmResource realmResource = keycloakAdmin.realm(realm);

            //check user exist
            checkExistingUser(employee.getEmail(), employee.getUsername(), realmResource);

            UserRepresentation user = createUserRepresentation(employee);

            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);

            handleCreateUserResponse(response);

            return extractUserIdFromResponse(response);
        } catch (KeycloakException e) {
            log.error("Keycloak error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user creation", e);
            throw new KeycloakException(KEYCLOAK_USER_CREATION_FAILED, KEYCLOAK_USER_CREATION_FAILED, e);
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

    private void checkExistingUser(String email, String username, RealmResource realmResource) {
        // Check for existing user by email
        List<UserRepresentation> existingUsersByEmail = realmResource.users().search(email, true);
        if (!existingUsersByEmail.isEmpty()) {
            throw new KeycloakException(
                    errorCode.CONFLICT_ERROR,
                    String.format(KEYCLOAK_USER_ALREADY_EXISTS, email)
            );
        }

        // Check for existing user by username
        List<UserRepresentation> existingUsersByUsername = realmResource.users().search(username, true);
        for (UserRepresentation user : existingUsersByUsername) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                throw new KeycloakException(
                        errorCode.CONFLICT_ERROR,
                        String.format(KEYCLOAK_USER_ALREADY_EXISTS, username)
                );
            }
        }
    }


    private UserRepresentation createUserRepresentation(EmployeeRequestDto employee) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(employee.getUsername());
        user.setFirstName(employee.getFirstName());
        user.setLastName(employee.getLastName());
        user.setEmail(employee.getEmail());
        user.setEnabled(true);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("source", List.of("timesheet-app"));

        String employeeType = (employee.getEmployeeType() == null || employee.getEmployeeType().isBlank())
                ? "Employee"
                : employee.getEmployeeType();
        attributes.put("EmployeeType", List.of(employeeType));

        user.setAttributes(attributes);


        return user;
    }


    private void handleCreateUserResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            try {
                String errorBody = response.readEntity(String.class);
                JsonNode errorNode = new ObjectMapper().readTree(errorBody);
                String errorDetail = errorNode.path("error_description").asText(null);

                String message = errorDetail != null ? errorDetail : errorBody;
                throw new KeycloakException(KEYCLOAK_USER_CREATION_FAILED, KEYCLOAK_USER_CREATION_FAILED + ": " + message);
            } catch (IOException e) {
                throw new KeycloakException(KEYCLOAK_USER_CREATION_FAILED, KEYCLOAK_USER_CREATION_FAILED, e);
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
                    USERID_EXTRACTION_FAILED,
                    e
            );
        }
    }

    public void updateUserPassword(String userId, String newPassword) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            realmResource.users().get(userId).resetPassword(credential);
        } catch (Exception e) {
            log.error("Error updating user password", e);
            throw new KeycloakException(
                    errorCode.KEYCLOAK_CONNECTION_ERROR,
                    PASSWORD_UPDATE_FAILED + ": " + e.getMessage(),
                    e
            );
        }
    }

    public UserRepresentation getUserByUsername(String username) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        List<UserRepresentation> users = realmResource.users().search(username, true);
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public UserRepresentation getUserById(String id) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        return realmResource.users().get(id).toRepresentation();
    }

    public List<UserRepresentation> getAllUsers() {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        return realmResource.users().list();
    }

}
