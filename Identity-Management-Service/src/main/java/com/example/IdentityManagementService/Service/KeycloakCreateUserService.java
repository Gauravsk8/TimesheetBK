package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;
import com.example.IdentityManagementService.exceptions.KeycloakException;
import com.example.common.audit.AuditEvent;
import com.example.common.audit.AuditKafkaProducer;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.common.constants.errorCode.KEYCLOAK_USER_CREATION_FAILED;
import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakCreateUserService {

    private final Keycloak keycloakAdmin;
    private final AuditKafkaProducer auditKafkaProducer;
    private final EmailService emailService;

    @Value("${keycloak.realm}")
    private String realm;

    public Map<String, String> createUser(EmployeeRequestDto employee) {
        try {
            //To check Admin connection
            verifyAdminConnection();

            //To get into timesheet realm
            RealmResource realmResource = keycloakAdmin.realm(realm);

            //check user exist
            checkExistingUser(employee.getEmail(), employee.getEmployeeCode(), realmResource);

            UserRepresentation user = createUserRepresentation(employee);

            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);

            handleCreateUserResponse(response);

            String userId = extractUserIdFromResponse(response);

            String randomPassword = generateRandomPassword();

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(true);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(randomPassword);

            usersResource.get(userId).resetPassword(passwordCred);

            Map<String, String> variables = new HashMap<>();
            variables.put("firstName", employee.getFirstName());
            variables.put("username", employee.getEmployeeCode());
            variables.put("password", randomPassword);

            String emailSubject = "Welcome to the Company Portal";
            String emailBody = emailService.loadTemplate("EmailTemplate.txt", variables);
            emailService.sendEmail(employee.getEmail(), emailSubject, emailBody);

            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String actor = authentication != null ? authentication.getName() : "unknown";

                Map<String, Object> auditDetails = new HashMap<>();
                auditDetails.put("createdUserId", userId);
                auditDetails.put("username", employee.getEmployeeCode());
                auditDetails.put("email", employee.getEmail());
                auditDetails.put("firstName", employee.getFirstName());
                auditDetails.put("lastName", employee.getLastName());
                auditDetails.put("employeeType", employee.getEmployeeType());
                auditDetails.put("createdBy", actor);

                AuditEvent event = new AuditEvent(
                        "create-user-service",
                        actor,
                        "CreateUser",
                        Instant.now(),
                        auditDetails
                );
                auditKafkaProducer.sendAudit(event);
            } catch (Exception ex) {
                log.error("Audit Kafka send failed", ex);
            }
            Map<String, String> result = new HashMap<>();
            result.put("userId", userId);
            result.put("temporaryPassword", randomPassword);
            return result;
        } catch (KeycloakException e) {
            log.error("Keycloak error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user creation", e);
            throw new KeycloakException(KEYCLOAK_USER_CREATION_FAILED, KEYCLOAK_USER_CREATION_FAILED, e);
        }
    }
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8); // 8 chars random password
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

    private void checkExistingUser(String email, String employeeCode, RealmResource realmResource) {
        // Check for existing user by employeeCode
        List<UserRepresentation> usersByemployeeCode = realmResource.users().search(employeeCode, 0, 1, true);
        for (UserRepresentation user : usersByemployeeCode) {
            if (user.getUsername().equalsIgnoreCase(employeeCode)) {
                throw new KeycloakException(
                        errorCode.CONFLICT_ERROR,
                        String.format(KEYCLOAK_USER_ALREADY_EXISTS, employeeCode)
                );
            }
        }

        // Check for existing user by email
        List<UserRepresentation> allUsers = realmResource.users().list(); // optionally paginate for performance
        for (UserRepresentation user : allUsers) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                throw new KeycloakException(
                        errorCode.CONFLICT_ERROR,
                        String.format(KEYCLOAK_USER_ALREADY_EXISTS, email)
                );
            }
        }
    }



    private UserRepresentation createUserRepresentation(EmployeeRequestDto employee) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(employee.getEmployeeCode());
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

    public UserRepresentation getUserByemployeeCode(String employeeCode) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        List<UserRepresentation> users = realmResource.users().search(employeeCode, true);
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(employeeCode))
                .findFirst()
                .orElseThrow(() -> new KeycloakException(errorCode.NOT_FOUND_ERROR, USER_NOT_FOUND + employeeCode));
    }

    public UserRepresentation getUserById(String id) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        return realmResource.users().get(id).toRepresentation();
    }

    public List<UserRepresentation> getAllUsers() {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        return realmResource.users().list();
    }

    public void updateUserProfile(String userId, EmployeeRequestDto employeeRequestDto) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = usersResource.get(userId).toRepresentation();

            if (employeeRequestDto.getFirstName() != null) {
                user.setFirstName(employeeRequestDto.getFirstName());
            }
            if (employeeRequestDto.getLastName() != null) {
                user.setLastName(employeeRequestDto.getLastName());
            }
            if (employeeRequestDto.getEmail() != null) {
                user.setEmail(employeeRequestDto.getEmail());
            }

            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            if (employeeRequestDto.getEmployeeType() != null) {
                attributes.put("EmployeeType", List.of(employeeRequestDto.getEmployeeType()));
            }
            user.setAttributes(attributes);

            usersResource.get(userId).update(user);

        } catch (Exception e) {
            log.error("Error updating user profile", e);
            throw new KeycloakException(
                    errorCode.KEYCLOAK_USER_UPDATE_FAILED,
                    USER_UPDATE_FAILED + e.getMessage(),
                    e
            );
        }
    }


}
