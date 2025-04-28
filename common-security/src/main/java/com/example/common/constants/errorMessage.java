package com.example.common.constants;

public class errorMessage {
    public static final String MISSING_BEARER_TOKEN = "Missing Bearer token";
    public static final String UNAUTHORIZED_ACCESS = "User not authorized to access the resource";


    // === Employee Errors ===
    public static final String EMPLOYEE_ALREADY_EXISTS = "Employee with email already exists";
    public static final String EMPLOYEE_SAVE_FAILED = "Failed to save employee to database";
    public static final String EMPLOYEE_CREATION_FAILED_LOG = "Error creating employee";
    public static final String ROLE_ALREADY_ASSIGNED = "Role Already Assigned ";


    // === Role Errors ===
    public static final String ROLE_NOT_FOUND = "Role not found ";
    public static final String ROLE_ASSIGN_FAILED = "Role assign Failed";

    public static final String USER_NOT_FOUND = "User not found";

    // === Validation & General Errors ===
    public static final String KEYCLOAK_USER_ALREADY_EXISTS = "Keycloak User Already Exist %s";
    public static final String KEYCLOAK_ADMIN_CONNECTION_FAILED = "Key cloak Admin connection Failed";
    public static final String USER_LOOKUP_FAILED = "User Look up failed";
    public static final String PASSWORD_UPDATE_FAILED = "Password Update Failed";


    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred";
    public static final String SERVICE_UNAVAILABLE = "Service unavailable";

    // === Keycloak Errors ===
    public static final String MALFORMED_BEARER_TOKEN = "Access Token is Expired or Malformed";
    public static final String USERID_EXTRACTION_FAILED = "Failed To Extract User Id";

    public static final String ERROR_SAVING_DAILY_TIMESHEET="Error saving daily time sheet";
    public static final String ACCESS_DENIED_TO_EDIT_TIMESHEET="Access denied to edit the time sheet since it is already approved by the manager";
    public static final String UNEXPECTED_ERROR_WHILE_SAVING_DAILY_TIMESHEET="Unexpected error while saving daily time sheet";
    public static final String DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES="Daily time sheets not found for this employee between these dates";
    public static final String ERROR_SAVING_WEEKLY_TIMESHEET="Error saving weekly time sheet";
    public static final String WEEKLY_TIME_SHEET_NOT_FOUND="Weekly time sheet not found";
    public static final String DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES="Daily time sheets not found for this employee between these dates, unable to fetch weekly hours spent";
    public static final String EMPLOYEES_NOT_FOUND_UNDER_THIS_MANAGER="No employees found under this manager";
    public static final String ERROR_FETCHING_EMPLOYEE_DETAILS="Failed to fetch details for employee";
    public static String USER_UPDATE_FAILED = "Failed To update User";

    private errorMessage() {}
}
