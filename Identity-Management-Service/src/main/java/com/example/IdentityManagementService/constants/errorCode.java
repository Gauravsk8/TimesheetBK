package com.example.IdentityManagementService.constants;

public class errorCode {
    public static final String VALIDATION_ERROR = "TIMESHEET_VALIDATION_ERROR";
    public static final String INTERNAL_SERVER_ERROR = "TIMESHEET_INTERNAL_SERVER_ERROR";
    public static final String NOT_FOUND_ERROR = "TIMESHEET_NOT_FOUND_ERROR";
    public static final String CONFLICT_ERROR = "TIMESHEET_CONFLICT_ERROR";
    public static final String FORBIDDEN_ERROR = "TIMESHEET_FORBIDDEN_ERROR";
    public static final String UNAUTHORIZED_ERROR = "TIMESHEET_UNAUTHORIZED_ERROR";
    public static final String ROLE_ASSIGNMENT_FAILED = "ROLE_ASSIGNMENT_FAILED";
    public static final String KEYCLOAK_USER_CREATION_FAILED = "KEYCLOAK_USER_CREATION_FAILED";
    public static final String KEYCLOAK_RESPONSE_PARSING_ERROR = "KEYCLOAK_RESPONSE_PARSING_ERROR";
    public static String MissingBearerToken = "BEARER_TOKEN_MISSING";
    public static final String KEYCLOAK_CONNECTION_ERROR = "KEYCLOAK_CONNECTION_ERROR";


    errorCode() {}
}
