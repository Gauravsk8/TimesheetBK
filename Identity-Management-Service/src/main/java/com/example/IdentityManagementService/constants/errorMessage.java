package com.example.IdentityManagementService.constants;

public class errorMessage {
    public static final String MISSING_BEARER_TOKEN = "Missing Bearer token";
    public static final String UNAUTHORIZED_ACCESS = "User not authorized to access the resource";

    public static final String KEYCLOAK_USER_CREATION_FAILED = "Failed to create user in Keycloak";
    public static final String KEYCLOAK_USER_ALREADY_EXISTS = "User with email %s already exists in Keycloak";
    public static final String KEYCLOAK_ADMIN_CONNECTION_FAILED = "Failed to access Keycloak as admin";
    public static final String USER_LOOKUP_FAILED = "Error checking if user exists in Keycloak";
    public static final String ROLE_ASSIGNMENT_FAILED = "Error assigning realm role to Keycloak user";

    public static final String PASSWORD_UPDATE_FAILED = "Failed to update Keycloak user password";
    public static final String MALFORMED_BEARER_TOKEN = "Access Token is Expired or Malformed";


    private errorMessage() {}
}
