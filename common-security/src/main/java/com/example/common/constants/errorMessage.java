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
    public static final String MANAGER_CODE_OR_MONTH_YEAR_MUST_NOT_BE_NULL = "Manager code or month/year must not be null.";
    public static final String EMPLOYEE_CODE_MUST_NOT_BE_NULL = "Employee code must not be null.";
    public static final String WEEK_START_OR_END_DATE_MUST_NOT_BE_NULL = "Week start date or end date must not be null.";
    public static final String EMPLOYEES_CAN_ONLY_VIEW_THEIR_TIMESHEET = "Employees can only view their own timesheet.";
    public static final String UNEXPECTED_ERROR_FETCHING_TIME_SHEET = "An unexpected error occurred while fetching the timesheet.";
    public static final String ERROR_CALCULATING_PROJECT_HOURS = "Error occurred while calculating project hours.";
    public static final String ERROR_CALCULATING_HOURS_SPENT = "Error occurred while calculating hours spent.";
    public static final String FAILED_TO_FETCH_EMPLOYEE_DETAILS = "Failed to fetch employee details.";
    public static final String FAILED_TO_APPROVE_WEEKLY_TIMESHEET = "Failed to approve weekly timesheet.";
    public static final String FAILED_TO_APPROVE_WEEKLY_TIMESHEET_WITH_MANAGER_OVERWRITE = "Failed to approve weekly timesheet with manager override.";
    public static final String DAILY_TIME_SHEET_CANNOT_BE_NULL = "Daily timesheet cannot be null.";
    public static final String ERROR_ADDING_DAILY_TIMESHEET = "Error occurred while adding daily timesheet.";
    public static final String ERROR_ADDING_PROJECT_ENTRY = "Error occurred while adding project entry.";
    public static final String TOTAL_PROJECT_HOURS_CANNOT_BE_NULL = "Total project hours cannot be null.";
    public static final String NO_EMPLOYEES_FOUND_REPORTING_TO_THIS_MANAGER = "No employees found reporting to this manager.";
    public static final String INVALID_MONTH_YEAR_FORMAT_MESSAGE = "Invalid month/year format. Expected format is MM-yyyy.";
    public static final String NO_VALID_WEEKLY_RANGES_FOR_SELECTED_MONTH = "No valid weekly ranges found for the selected month.";
    public static final String USER_UPDATE_FAILED = "User update failed";
    public static final String CLIENT_NOT_FOUND="Client not found with id: %s";
    public static final String COST_CENTER_NOT_FOUND = "Cost center not found with code: %s";
    public static final String PROJECT_NOT_FOUND = "Project not found with code: %s";
    public static final String PROJECT_ALREADY_EXISTS = "Project with code '%s' already exists";
    public static final String ASSIGNMENT_NOT_FOUND = "Assignment not found for project '%s' and employee '%s'";
    public static final String PROJECT_MANAGER_ROLE = "Manager %s does not have project manager role";
    public static final String CostCenterManager_MANAGER_ROLE = "Manager %s does not have CostCenter manager role";

    public static final String ReportingManagerAssignedFAILED ="Failed To assign Reporting Manager to employee";
    public static final String ReportingManagerAssigned ="Reporting manager is assigned to employee";



    private errorMessage() {}
}
