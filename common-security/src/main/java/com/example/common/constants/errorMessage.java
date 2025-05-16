package com.example.common.constants;

public class errorMessage {
    public static final String MISSING_BEARER_TOKEN = "Missing Bearer token";
    public static final String UNAUTHORIZED_ACCESS = "User not authorized to access the resource";


    // === Employee Errors ===
    public static final String EMPLOYEE_ALREADY_EXISTS = "Employee already exists with ";
    public static final String EMPLOYEE_SAVE_FAILED = "Failed to save employee to database";
    public static final String EMPLOYEE_CREATION_FAILED_LOG = "Error creating employee";
    public static final String ROLE_ALREADY_ASSIGNED = "Role Already Assigned ";


    // === Role Errors ===
    public static final String ROLE_NOT_FOUND = "Role not found ";
    public static final String ROLE_NOT_ASSIGNED = "Role Not Assigned";

    public static final String USER_NOT_FOUND = "User not found";

    // === Validation & General Errors ===
    public static final String KEYCLOAK_USER_ALREADY_EXISTS = "Keycloak User Already Exist %s";
    public static final String KEYCLOAK_ADMIN_CONNECTION_FAILED = "Key cloak Admin connection Failed";
    public static final String PASSWORD_UPDATE_FAILED = "Password Update Failed";



    // === Keycloak Errors ===
    public static final String MALFORMED_BEARER_TOKEN = "Access Token is Expired or Malformed";
    public static final String USERID_EXTRACTION_FAILED = "Failed To Extract User Id";


    public static final String DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES="Daily time sheets not found for this employee between these dates, unable to fetch weekly hours spent";


    public static final String USER_UPDATE_FAILED = "User update failed";
    public static final String CLIENT_NOT_FOUND="Client not found with id: %s";
    public static final String COST_CENTER_NOT_FOUND = "Cost center not found with code: %s";
    public static final String PROJECT_NOT_FOUND = "Project not found with code: %s";
    public static final String PROJECT_ALREADY_EXISTS = "Project with code '%s' already exists";
    public static final String ASSIGNMENT_NOT_FOUND = "Assignment not found for project '%s' and employee '%s'";


    public static final String ReportingManagerAssignedFAILED ="Failed To assign Reporting Manager to employee";
    public static final String ReportingManagerAssigned ="Reporting manager is assigned to employee";
    public static final String NO_MANAGER_ASSIGNED = "No Manager Assigned";
    public static final String STATUS_NOT_FOUND =  "Status not found ";
    public static final String RM_NOT_ASSIGNED = "Reporting manager not assigned for employee: " ;
    public static final String RM_NOT_FOUND = "Reporting manager not found" ;


    //Timesheet

    public static final String TIMESHEET_SUMMARY_NOT_FOUND="No weekly summary found for employee %s (week %s)";



    private errorMessage() {}
}
