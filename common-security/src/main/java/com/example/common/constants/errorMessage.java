package com.example.common.constants;

public class errorMessage {
    public static final String MISSING_BEARER_TOKEN = "Missing Bearer token";
    public static final String UNAUTHORIZED_ACCESS = "User not authorized to access the resource";


    // === Employee Errors ===
    public static final String EMPLOYEE_ALREADY_EXISTS = "Employee with email already exists";
    public static final String EMPLOYEE_SAVE_FAILED = "Failed to save employee to database";
    public static final String EMPLOYEE_CREATION_SUCCESS = "Employee created successfully with ID: %s";
    public static final String EMPLOYEE_CREATION_FAILED_LOG = "Error creating employee";

    // === Role Errors ===
    public static final String ROLE_NOT_FOUND = "Role not found";

    // === Validation & General Errors ===
    public static final String KEYCLOAK_USER_ALREADY_EXISTS = "Keycloak User Already Exist %s";
    public static final String KEYCLOAK_ADMIN_CONNECTION_FAILED = "Key cloak Admin connection Failed";
    public static final String USER_LOOKUP_FAILED = "User Look up failed";
    public static final String PASSWORD_UPDATE_FAILED = "Password Update Failed";


    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred";
    public static final String SERVICE_UNAVAILABLE = "Service unavailable";

    // === Keycloak Errors ===
    public static final String MALFORMED_BEARER_TOKEN = "Access Token is Expired or Malformed";


    private errorMessage() {}
}
