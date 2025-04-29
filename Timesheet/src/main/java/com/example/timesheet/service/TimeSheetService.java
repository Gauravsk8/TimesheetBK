package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.EmployeeReportingManagerRepository;
import com.example.timesheet.Repository.ProjectTimeEntryRepository;
import com.example.timesheet.Repository.WeeklyTimeSheetRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.dto.request.*;
import com.example.timesheet.enums.TimeSheetStatus;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.EmployeeReportingManager;
import com.example.timesheet.models.ProjectTimeEntry;
import com.example.timesheet.models.WeeklyTimeSheet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static com.example.common.constants.errorCode.*;
import static com.example.common.constants.errorCode.INTERNAL_SERVER_ERROR;
import static com.example.common.constants.errorCode.INVALID_MONTH_YEAR_FORMAT;
import static com.example.common.constants.errorMessage.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TimeSheetService {

    private final DailyTimeSheetRepository dailyTimeSheetRepository;
    private final ProjectTimeEntryRepository projectTimeEntryRepository;
    private final WeeklyTimeSheetRepository weeklyTimeSheetRepository;
    private final EmployeeReportingManagerRepository employeeReportingManagerRepository;
    private final IdentityServiceClient identityServiceClient;
    public String enterOrUpdateDailyTimeSheet(List<DailyTimeSheetRequest> dailyTimeSheetRequests) {
        for (DailyTimeSheetRequest dailyTimeSheetRequest : dailyTimeSheetRequests) {
            try{
            Long totalHours = 0L;
            TimeSheetStatus status = null;
            DailyTimeSheet dailyTimeSheet = dailyTimeSheetRepository.findByDateAndEmployeeCode(dailyTimeSheetRequest.getDate(), dailyTimeSheetRequest.getEmployeeCode());

            if (dailyTimeSheet == null) {
                dailyTimeSheet = new DailyTimeSheet();
                status = TimeSheetStatus.OPEN;// if not found, create new
            } else {
                if (dailyTimeSheet.getWeeklyTimeSheet().getTimeSheetStatus() == TimeSheetStatus.OPEN) {
                    status = TimeSheetStatus.OPEN;
                    projectTimeEntryRepository.deleteByDailyTimeSheetId(dailyTimeSheet.getId());
                }
                else {
                    throw  new TimeSheetException(ACCESS_DENIED,ACCESS_DENIED_TO_EDIT_TIMESHEET);
                }
                // Delete all existing project entries for the current dailyTimeSheet
            }
            if (status == TimeSheetStatus.OPEN) {
//                dailyTimeSheet.setDate(dailyTimeSheetRequest.getDate());
//                dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());
//                dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());
//                dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());
//                dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());
//                //dailyTimeSheet.setTotalHours(dailyTimeSheetRequest.getTotalHours());
//                dailyTimeSheet.setEmployeeId(dailyTimeSheetRequest.getEmployeeId());
//                dailyTimeSheet.setEmployeeCode(dailyTimeSheetRequest.getEmployeeCode());
                addDailyTimeSheet(dailyTimeSheet, dailyTimeSheetRequest);
                DailyTimeSheet savedDailyTimeSheet = dailyTimeSheetRepository.save(dailyTimeSheet);
                if (savedDailyTimeSheet.getId() == null) {
                    throw new TimeSheetException(SAVE_ERROR, ERROR_SAVING_DAILY_TIMESHEET);
                }
                totalHours = calculateTotalLoggedHours(dailyTimeSheetRequest);

                // Create new project entries
                List<ProjectTimeEntry> newEntries = new ArrayList<>();
                for (ProjectTimeSheetEntryRequest e : dailyTimeSheetRequest.getProjectTimeSheetEntryRequests()) {
                    ProjectTimeEntry entry = new ProjectTimeEntry();
//                    entry.setProjectId(e.getProjectId());
//                    entry.setTotalHoursSpent(e.getTotalHoursSpent());
//                    entry.setDailyTimeSheet(savedDailyTimeSheet);
                    addProjectTimeSheetEntry(entry, e, savedDailyTimeSheet);
                    totalHours += e.getTotalHoursSpent();
                    newEntries.add(entry);
                }
                dailyTimeSheet.setTotalHours(totalHours);

                projectTimeEntryRepository.saveAll(newEntries);
            }
        }catch (TimeSheetException e) {
            // Re-throw custom exception if explicitly thrown
            throw e;
        } catch (Exception e) {
            // Wrap and throw any other unexpected exceptions
            throw new TimeSheetException(SAVE_ERROR, UNEXPECTED_ERROR_WHILE_SAVING_DAILY_TIMESHEET, e);
        }

        }
        return "Saved";
    }


    public String weeklyTimeSheetEntry(WeeklyTimeSheetRequest weeklyTimeSheetRequest) {
        try {
            System.out.println("Week start date: " + weeklyTimeSheetRequest.getWeekStartDate());

            if (weeklyTimeSheetRequest.getEmployeeCode() == null || weeklyTimeSheetRequest.getEmployeeCode().isEmpty()) {
                throw new TimeSheetException(INVALID_INPUT, EMPLOYEE_CODE_MUST_NOT_BE_NULL);
            }

            if (weeklyTimeSheetRequest.getWeekStartDate() == null || weeklyTimeSheetRequest.getWeekEndDate() == null) {
                throw new TimeSheetException(INVALID_INPUT, WEEK_START_OR_END_DATE_MUST_NOT_BE_NULL);
            }

            List<DailyTimeSheet> dailySheets = dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(
                    weeklyTimeSheetRequest.getEmployeeCode(),
                    weeklyTimeSheetRequest.getWeekStartDate(),
                    weeklyTimeSheetRequest.getWeekEndDate()
            );

            if (dailySheets == null || dailySheets.isEmpty()) {
                throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES);
            }

            System.out.println("Size of daily sheets: " + dailySheets.size());
            System.out.println("Start Date (request): " + weeklyTimeSheetRequest.getWeekStartDate());
            System.out.println("Start Date (DB): " + dailySheets.get(0).getDate());

            WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findByWeekStartDateAndEmployeeCode(
                    weeklyTimeSheetRequest.getWeekStartDate(),
                    weeklyTimeSheetRequest.getEmployeeCode()
            );

            if (weeklyTimeSheet == null) {
                weeklyTimeSheet = new WeeklyTimeSheet(); // Create a new one if not found
            }

            weeklyTimeSheet.setEmployeeId(weeklyTimeSheetRequest.getEmployeeId());
            weeklyTimeSheet.setEmployeeCode(weeklyTimeSheetRequest.getEmployeeCode());
            weeklyTimeSheet.setWeekStartDate(weeklyTimeSheetRequest.getWeekStartDate());

            Long totalHours = 0L;
            for (DailyTimeSheet daily : dailySheets) {
                totalHours += daily.getTotalHours();
                daily.setWeeklyTimeSheet(weeklyTimeSheet);
            }

            weeklyTimeSheet.setTotalWorkingHours(totalHours);
            weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.PENDING_APPROVAL);

            if (weeklyTimeSheet.getDailySheets() == null) {
                weeklyTimeSheet.setDailySheets(new ArrayList<>());
            } else {
                weeklyTimeSheet.getDailySheets().clear();
            }

            for (DailyTimeSheet daily : dailySheets) {
                daily.setWeeklyTimeSheet(weeklyTimeSheet);
                weeklyTimeSheet.getDailySheets().add(daily);
            }

            WeeklyTimeSheet savedWeeklyTimeSheet = weeklyTimeSheetRepository.save(weeklyTimeSheet);

            if (savedWeeklyTimeSheet == null || savedWeeklyTimeSheet.getId() == null) {
                throw new TimeSheetException(SAVE_ERROR, ERROR_SAVING_WEEKLY_TIMESHEET + weeklyTimeSheetRequest.getEmployeeCode());
            }

            return "Saved";

        } catch (TimeSheetException ex) {
            // Custom application exceptions, rethrow as is
            throw ex;
        } catch (Exception ex) {
            // Unexpected exceptions
            ex.printStackTrace(); // Ideally use logger here
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_SAVING_DAILY_TIMESHEET);
        }
    }
    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInEmployeeCode = null;

            if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
                Jwt jwt = jwtAuthToken.getToken();
                loggedInEmployeeCode = jwt.getClaimAsString("preferred_username"); // or "email"
            }

            System.out.println("Logged-in employee: " + loggedInEmployeeCode);

            boolean isManager = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("SCOPE_view_all_timesheets"));

            if (!isManager && !employeeCode.equals(loggedInEmployeeCode)) {
                throw new TimeSheetException(ACCESS_DENIED, EMPLOYEES_CAN_ONLY_VIEW_THEIR_TIMESHEET);
            }

            System.out.println("Week start date in timesheet service: " + weekStartDate);

            WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository
                    .findByEmployeeCodeAndWeekStartDate(employeeCode, weekStartDate);

            if (weeklyTimeSheet == null) {
                throw new TimeSheetException(NOT_FOUND_ERROR, WEEKLY_TIME_SHEET_NOT_FOUND + employeeCode);
            }

            WeeklyTimeSheetResponse weeklyTimeSheetResponse = new WeeklyTimeSheetResponse();
            weeklyTimeSheetResponse.setId(weeklyTimeSheet.getId());
            weeklyTimeSheetResponse.setEmployeeId(weeklyTimeSheet.getEmployeeId());
            weeklyTimeSheetResponse.setTotalWorkingHours(weeklyTimeSheet.getTotalWorkingHours());
            weeklyTimeSheetResponse.setTotalIdleHours(weeklyTimeSheet.getTotalIdleHours()); // Fixed mistake: using getter

            // Calculate total project hours
            Map<Long, Long> projectHours = calculateTotalProjectHours(employeeCode, weekStartDate, weekEndDate, weeklyTimeSheet.getDailySheets());
            weeklyTimeSheetResponse.setProjectHours(projectHours);

            // Build list of daily time sheet responses
            List<DailyTimeSheetResponse> dailyTimeSheetResponses = new ArrayList<>();
            for (DailyTimeSheet dailyTimeSheet : weeklyTimeSheet.getDailySheets()) {
                dailyTimeSheetResponses.add(convertToDailyTimeSheetResponse(dailyTimeSheet));
            }
            weeklyTimeSheetResponse.setDailyTimeSheetResponses(dailyTimeSheetResponses);

            // Get hours by type
            Map<String, Long> hoursMap = getWeeklyHoursSpentByType(employeeCode, weekStartDate, weekEndDate);
            weeklyTimeSheetResponse.setHoursMap(hoursMap);

            return weeklyTimeSheetResponse;

        } catch (TimeSheetException ex) {
            throw ex; // Custom business exception, propagate as-is
        } catch (Exception ex) {
            ex.printStackTrace(); // or use log.error("Error fetching weekly timesheet", ex);
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_FETCHING_TIME_SHEET);
        }
    }




    // Calculate total project hours spent in the week, per project

    private Map<Long, Long> calculateTotalProjectHours(String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate, List<DailyTimeSheet> dailyTimeSheets) {
        try {
            if (dailyTimeSheets == null || dailyTimeSheets.isEmpty()) {
                throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES);
            }

            Map<Long, Long> projectHoursMap = new HashMap<>();

            for (DailyTimeSheet dailySheet : dailyTimeSheets) {
                List<ProjectTimeEntry> projectEntries = projectTimeEntryRepository.findByDailyTimeSheetId(dailySheet.getId());

                for (ProjectTimeEntry entry : projectEntries) {
                    Long projectId = entry.getProjectId();
                    Long totalHours = entry.getTotalHoursSpent() != null ? entry.getTotalHoursSpent() : 0L;
                    projectHoursMap.put(projectId, projectHoursMap.getOrDefault(projectId, 0L) + totalHours);
                }
            }

            return projectHoursMap;

        } catch (TimeSheetException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace(); // optionally use logger
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_CALCULATING_PROJECT_HOURS);
        }
    }


    public Long getWeeklyHoursSpent(Long projectId, String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate) {
        try {
            List<DailyTimeSheet> dailyTimeSheets = dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(employeeCode, weekStartDate, weekEndDate);

            if (dailyTimeSheets == null || dailyTimeSheets.isEmpty()) {
                throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES);
            }

            long totalHours = 0L;

            for (DailyTimeSheet dailySheet : dailyTimeSheets) {
                List<ProjectTimeEntry> projectEntries = projectTimeEntryRepository
                        .findByDailyTimeSheetIdAndProjectId(dailySheet.getId(), projectId);

                for (ProjectTimeEntry entry : projectEntries) {
                    totalHours += (entry.getTotalHoursSpent() != null ? entry.getTotalHoursSpent() : 0L);
                }
            }

            return totalHours;

        } catch (TimeSheetException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace(); // You can replace this with a proper logger
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_CALCULATING_HOURS_SPENT);
        }
    }


    public Map<String, Long> getWeeklyHoursSpentByType(String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate) {
        try {
            List<DailyTimeSheet> dailyTimeSheets = dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(employeeCode, weekStartDate, weekEndDate);

            if (dailyTimeSheets == null || dailyTimeSheets.isEmpty()) {
                throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES);
            }

            Map<String, Long> hoursMap = new HashMap<>();

            for (DailyTimeSheet dailyTimeSheet : dailyTimeSheets) {
                hoursMap.put("holiday", hoursMap.getOrDefault("holiday", 0L) + dailyTimeSheet.getHoliday());
                hoursMap.put("idle", hoursMap.getOrDefault("idle", 0L) + dailyTimeSheet.getIdeal());
                hoursMap.put("leave", hoursMap.getOrDefault("leave", 0L) + dailyTimeSheet.getLeave());
                hoursMap.put("training", hoursMap.getOrDefault("training", 0L) + dailyTimeSheet.getTraining());
            }

            return hoursMap;

        } catch (TimeSheetException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace(); // Consider using a logger instead
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_CALCULATING_HOURS_SPENT);
        }
    }




    public List<UserIdentityDto> getAllEmployees(String managerCode) {
        List<EmployeeReportingManager> employeeReportingManagerList =
                employeeReportingManagerRepository.findByManagerCode(managerCode);

        if (employeeReportingManagerList == null || employeeReportingManagerList.isEmpty()) {
            throw new TimeSheetException(NOT_FOUND_ERROR, EMPLOYEES_NOT_FOUND_UNDER_THIS_MANAGER);
        }

        List<UserIdentityDto> employeeDetails = new ArrayList<>();

        for (EmployeeReportingManager report : employeeReportingManagerList) {
            String employeeCode = report.getEmployeeCode();

            try {
                ResponseEntity<UserIdentityDto> response = identityServiceClient.getUserByemployeeCode(employeeCode);

                if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    employeeDetails.add(response.getBody());
                } else {
                    throw new TimeSheetException(FAILED_TO_FETCH_DETAILS,
                            FAILED_TO_FETCH_EMPLOYEE_DETAILS + employeeCode);
                }

            } catch (TimeSheetException e) {
                throw e; // rethrow specific known exception
            } catch (Exception e) {
                e.printStackTrace(); // or use proper logger
                throw new TimeSheetException(FAILED_TO_FETCH_DETAILS,
                        FAILED_TO_FETCH_EMPLOYEE_DETAILS + employeeCode);
            }
        }

        return employeeDetails;
    }

    public String approvedByManager(Long weeklyTimeSheetId, String managerCode) {
        try {
            WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
                    .orElseThrow(() -> new TimeSheetException(NOT_FOUND_ERROR, WEEKLY_TIME_SHEET_NOT_FOUND));

            weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.APPROVED);
            weeklyTimeSheet.setApprovedBy(managerCode); // Set approved by manager code

            weeklyTimeSheetRepository.save(weeklyTimeSheet);

            return "Weekly time sheet approved successfully.";
        } catch (TimeSheetException e) {
            throw e; // Rethrow custom exception if already known
        } catch (Exception e) {
            e.printStackTrace(); // Ideally, use logger.error instead of printStackTrace
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, FAILED_TO_APPROVE_WEEKLY_TIMESHEET);
        }
    }

    public String approveWithManagerOverWrite(ApproveWithManagerOverWriteRequest approveWithManagerOverWriteRequest, Long weeklyTimeSheetId, TimeSheetStatus status) {
        try {
            // Fetch daily timesheets for the specified employee and date range
            List<DailyTimeSheet> dailyTimeSheets = dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(approveWithManagerOverWriteRequest.getEmployeeCode(), approveWithManagerOverWriteRequest.getWeekStartDate(), approveWithManagerOverWriteRequest.getWeekEndDate());
            if (dailyTimeSheets.isEmpty()) {
                throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES);
            }

            Long weeklyHoursSpent = 0L;
            System.out.println("Daily time sheet in manager overwrite:" + dailyTimeSheets.size());

            for (DailyTimeSheet dailyTimeSheet : dailyTimeSheets) {
                for (DailyTimeSheetRequest dailyTimeSheetRequest : approveWithManagerOverWriteRequest.getDailyTimeSheetRequests()) {
                    if (dailyTimeSheet.getDate().equals(dailyTimeSheetRequest.getDate())) {
                        Long totalHours = dailyTimeSheet.getTotalHours();
                        Long oldHoliday = dailyTimeSheet.getHoliday();
                        Long oldTraining = dailyTimeSheet.getTraining();
                        Long oldIdle = dailyTimeSheet.getIdeal();
                        Long oldLeave = dailyTimeSheet.getLeave();

                        System.out.println("Matched:" + dailyTimeSheet.getDate());

                        // Handle changes in holiday hours
                        if (!dailyTimeSheet.getHoliday().equals(dailyTimeSheetRequest.getHoliday())) {
                            totalHours -= oldHoliday;
                            totalHours += dailyTimeSheetRequest.getHoliday();
                            dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());
                            dailyTimeSheet.setHolidayModifiedByManager(true);
                        }

                        // Handle changes in training hours
                        if (!dailyTimeSheet.getTraining().equals(dailyTimeSheetRequest.getTraining())) {
                            totalHours -= oldTraining;
                            totalHours += dailyTimeSheetRequest.getTraining();
                            dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());
                            dailyTimeSheet.setTrainingModifiedByManager(true);
                        }

                        // Handle changes in idle hours
                        if (!dailyTimeSheet.getIdeal().equals(dailyTimeSheetRequest.getIdeal())) {
                            totalHours -= oldIdle;
                            totalHours += dailyTimeSheetRequest.getIdeal();
                            dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());
                            dailyTimeSheet.setIdealModifiedByManager(true);
                        }

                        // Handle changes in leave hours
                        if (!dailyTimeSheet.getLeave().equals(dailyTimeSheetRequest.getLeave())) {
                            totalHours -= oldLeave;
                            totalHours += dailyTimeSheetRequest.getLeave();
                            dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());
                            dailyTimeSheet.setLeaveModifiedByManager(true);
                        }

                        // Handle project time entry updates
                        for (ProjectTimeEntry projectTimeEntry : dailyTimeSheet.getProjectTimeEntries()) {
                            Long oldProjectHours = projectTimeEntry.getTotalHoursSpent();
                            System.out.println("Old project hours:" + oldProjectHours);

                            for (ProjectTimeSheetEntryRequest projectTimeSheetEntryRequest : dailyTimeSheetRequest.getProjectTimeSheetEntryRequests()) {
                                if (projectTimeEntry.getProjectId().equals(projectTimeSheetEntryRequest.getProjectId())) {
                                    if (!projectTimeEntry.getTotalHoursSpent().equals(projectTimeSheetEntryRequest.getTotalHoursSpent())) {
                                        totalHours -= oldProjectHours;
                                        totalHours += projectTimeSheetEntryRequest.getTotalHoursSpent();
                                        System.out.println("Old project hour:" + totalHours);
                                        projectTimeEntry.setTotalHoursSpent(projectTimeSheetEntryRequest.getTotalHoursSpent());
                                        projectTimeEntry.setTotalHoursSpentOnProjectModifiedByManager(true);
                                    }
                                }
                            }
                        }

                        System.out.println("Total hours:" + totalHours);
                        dailyTimeSheet.setTotalHours(totalHours);
                    }
                }
                weeklyHoursSpent += dailyTimeSheet.getTotalHours();
                dailyTimeSheetRepository.save(dailyTimeSheet);  // Save updated daily time sheet
            }

            // Fetch and update the weekly timesheet
            WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
                    .orElseThrow(() -> new TimeSheetException(NOT_FOUND_ERROR, WEEKLY_TIME_SHEET_NOT_FOUND));

            weeklyTimeSheet.setTimeSheetStatus(status);
            weeklyTimeSheet.setTotalWorkingHours(weeklyHoursSpent);
            weeklyTimeSheet.setCommentsByManager(approveWithManagerOverWriteRequest.getCommentsByManager());

            return status == TimeSheetStatus.MANAGER_APPROVED ? "Approved and saved manager overwritten changes" : "Manager sent back weekly timesheet";
        } catch (TimeSheetException e) {
            throw e; // Rethrow custom TimeSheetException if already thrown inside the method
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging (or replace with logger)
            throw new TimeSheetException(INTERNAL_SERVER_ERROR,FAILED_TO_APPROVE_WEEKLY_TIMESHEET_WITH_MANAGER_OVERWRITE);
        }
    }

//
//    public String sendBackWeeklyTimSheetByManager(Long weeklyTimeSheetId,String commentsByManager) {
//        WeeklyTimeSheet weeklyTimeSheet=weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
//                .orElseThrow(()->new RuntimeException("No weekly time sheets found for that id"));
//        weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.OPEN);
//        weeklyTimeSheet.setCommentsByManager(commentsByManager);
//        return "Manager sent back weekly timesheet";
//    }

//    public ShiftDetailsResponse shiftDetailsOfEmployee(Long id) {
//        ShiftDetailsResponse shiftDetailsResponse=new ShiftDetailsResponse();
//        shiftDetailsResponse.setStartDay(DayOfWeek.MONDAY);
//        shiftDetailsResponse.setEndDay(DayOfWeek.FRIDAY);
//        shiftDetailsResponse.setStartTime(LocalTime.of(10,0));
//        shiftDetailsResponse.setEndTime(LocalTime.of(19,0));
//        return shiftDetailsResponse;
//    }

    private void addDailyTimeSheet(DailyTimeSheet dailyTimeSheet, DailyTimeSheetRequest dailyTimeSheetRequest) {
        try {
            // Validate incoming data
            if (dailyTimeSheetRequest == null) {
                throw new TimeSheetException(NOT_NULL,DAILY_TIME_SHEET_CANNOT_BE_NULL);
            }

            // Set values for dailyTimeSheet from dailyTimeSheetRequest
            dailyTimeSheet.setDate(dailyTimeSheetRequest.getDate());
            dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());
            dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());
            dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());
            dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());

            // Note: Total hours commented out since the line was originally commented
            // dailyTimeSheet.setTotalHours(dailyTimeSheetRequest.getTotalHours());

            dailyTimeSheet.setEmployeeId(dailyTimeSheetRequest.getEmployeeId());
            dailyTimeSheet.setEmployeeCode(dailyTimeSheetRequest.getEmployeeCode());

            // Log the successful addition
            System.out.println("Successfully added DailyTimeSheet for employee: " + dailyTimeSheetRequest.getEmployeeCode() + " on date: " + dailyTimeSheetRequest.getDate());

        } catch (IllegalArgumentException e) {
            // Log invalid argument exceptions, which may indicate invalid input
            System.err.println("Error: Invalid argument passed to addDailyTimeSheet: " + e.getMessage());
            throw e; // Rethrow the exception if necessary
        } catch (Exception e) {
            // Catch any other exceptions
            e.printStackTrace(); // Log the stack trace for debugging purposes
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_ADDING_DAILY_TIMESHEET);
        }
    }


    private void addProjectTimeSheetEntry(ProjectTimeEntry entry, ProjectTimeSheetEntryRequest e, DailyTimeSheet savedDailyTimeSheet) {
        try {
            // Validate the input
            if (entry == null || e == null || savedDailyTimeSheet == null) {
                throw new TimeSheetException(NOT_NULL, DAILY_TIME_SHEET_CANNOT_BE_NULL);
            }

            // Set values for project time entry
            entry.setProjectId(e.getProjectId());
            entry.setTotalHoursSpent(e.getTotalHoursSpent());
            entry.setDailyTimeSheet(savedDailyTimeSheet);

            // Log the successful addition
            System.out.println("Successfully added ProjectTimeSheetEntry for project ID: " + e.getProjectId() + " with total hours: " + e.getTotalHoursSpent());

        } catch (IllegalArgumentException ex) {
            // Log invalid argument exceptions
            System.err.println("Error: Invalid argument passed to addProjectTimeSheetEntry: " + ex.getMessage());
            throw ex;  // Rethrow the exception if necessary
        } catch (Exception ex) {
            // Log any other exceptions
            ex.printStackTrace();  // Log the stack trace for debugging
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, ERROR_ADDING_PROJECT_ENTRY);
        }
    }

    private DailyTimeSheetResponse convertToDailyTimeSheetResponse(DailyTimeSheet dailyTimeSheet) {
        // Check if the input is null
        if (dailyTimeSheet == null) {
            throw new TimeSheetException(NOT_NULL,DAILY_TIME_SHEET_CANNOT_BE_NULL );
        }

        // Create the response object
        DailyTimeSheetResponse response = new DailyTimeSheetResponse();

        // Set simple fields directly
        response.setEmployeeCode(dailyTimeSheet.getEmployeeCode());
        response.setIdeal(dailyTimeSheet.getIdeal());
        response.setDate(dailyTimeSheet.getDate());
        response.setHoliday(dailyTimeSheet.getHoliday());
        response.setLeave(dailyTimeSheet.getLeave());
        response.setTraining(dailyTimeSheet.getTraining());
        response.setId(dailyTimeSheet.getId());
        response.setEmployeeId(dailyTimeSheet.getEmployeeId());
        response.setTotalHours(dailyTimeSheet.getTotalHours());

        // Handle ProjectTimeEntry responses, ensuring we handle potential nulls
        List<ProjectTimeSheetEntryResponse> projectResponses = new ArrayList<>();

        if (dailyTimeSheet.getProjectTimeEntries() != null) {
            for (ProjectTimeEntry entry : dailyTimeSheet.getProjectTimeEntries()) {
                if (entry != null) {
                    ProjectTimeSheetEntryResponse projectResponse = new ProjectTimeSheetEntryResponse();
                    projectResponse.setId(entry.getId());
                    projectResponse.setProjectId(entry.getProjectId());
                    projectResponse.setTotalHoursSpent(entry.getTotalHoursSpent());

                    // Add to project responses list
                    projectResponses.add(projectResponse);
                } else {
                    // Optionally log or handle null entries if you expect that case
                    System.err.println("Null ProjectTimeEntry found for DailyTimeSheet ID: " + dailyTimeSheet.getId());
                }
            }
        }

        // Set project time entry responses
        response.setProjectTimeSheetEntryResponses(projectResponses);

        // Return the populated response
        return response;
    }
    private Long calculateTotalLoggedHours(DailyTimeSheetRequest req) {
        long total = 0;

        try {
            // Check if the request object is valid
            if (req == null) {
                throw new TimeSheetException(NOT_NULL,DAILY_TIME_SHEET_CANNOT_BE_NULL);
            }

            // Add basic hours (Leave, Holiday, Training, Ideal)
            total += req.getLeave() + req.getHoliday() + req.getTraining() + req.getIdeal();

            // Add project hours if available
            if (req.getProjectTimeSheetEntryRequests() != null) {
                for (ProjectTimeSheetEntryRequest projectReq : req.getProjectTimeSheetEntryRequests()) {
                    // Ensure that the total hours spent for each project entry is non-negative
                    if (projectReq.getTotalHoursSpent() < 0) {
                        throw new TimeSheetException(NOT_NULL,TOTAL_PROJECT_HOURS_CANNOT_BE_NULL);
                    }
                    total += projectReq.getTotalHoursSpent();
                }
            }

        } catch (IllegalArgumentException e) {
            // Handle invalid argument exceptions
            // For example, you could log this error and return a default value or rethrow the exception
            System.err.println("Error calculating total logged hours: " + e.getMessage());
            throw e;  // Rethrow or handle appropriately
        } catch (Exception e) {
            // Handle any other exceptions
            System.err.println("Unexpected error occurred while calculating total logged hours: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while calculating total logged hours.", e);
        }

        // Return the calculated total hours
        return total;
    }

    private boolean checkIfUserIsManager(String employeeCode){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmployeeCode = authentication != null ? authentication.getName():"Error";
        boolean isManager=false;
        List<String> role=identityServiceClient.getAssignedRoles(currentEmployeeCode);
        if(role.contains("MANAGER")){
            isManager=true;
        }
        if(!isManager && !employeeCode.equals(currentEmployeeCode)){
            throw new TimeSheetException(ACCESS_DENIED,"You can only view your time sheet");
        }
        return true;
    }


    public List<ManagerDashboardResponse> getManagerDashboardResponse(String managerCode, String monthYear) {
        List<ManagerDashboardResponse> managerDashboardResponses = new ArrayList<>();

        if (managerCode == null || managerCode.isEmpty() || monthYear == null || monthYear.isEmpty()) {
            throw new TimeSheetException(INVALID_INPUT, MANAGER_CODE_OR_MONTH_YEAR_MUST_NOT_BE_NULL);
        }

        List<EmployeeReportingManager> employeeReportingManagers = employeeReportingManagerRepository.findByManagerCode(managerCode);
        if (employeeReportingManagers == null || employeeReportingManagers.isEmpty()) {
            throw new TimeSheetException(NOT_FOUND_ERROR, NO_EMPLOYEES_FOUND_REPORTING_TO_THIS_MANAGER + managerCode);
        }

        Month month;
        int year;
        try {
            String[] parts = monthYear.split(" ");
            month = Month.valueOf(parts[0].toUpperCase());
            year = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            throw new TimeSheetException(INVALID_MONTH_YEAR_FORMAT, INVALID_MONTH_YEAR_FORMAT_MESSAGE);
        }

        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastOfMonth = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
        LocalDate currentMonday = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<LocalDate> weekStartDates = new ArrayList<>();

        while (currentMonday.isBefore(lastOfMonth.plusDays(1))) {
            int daysInMonth = 0;
            for (int i = 0; i < 7; i++) {
                LocalDate day = currentMonday.plusDays(i);
                if (day.getMonth() == month) {
                    daysInMonth++;
                }
            }

            if (daysInMonth >= 4) {
                weekStartDates.add(currentMonday);
            }

            currentMonday = currentMonday.plusWeeks(1);
        }

        if (weekStartDates.isEmpty()) {
            throw new TimeSheetException(NOT_FOUND_ERROR, NO_VALID_WEEKLY_RANGES_FOR_SELECTED_MONTH + monthYear);
        }

        for (EmployeeReportingManager employee : employeeReportingManagers) {
            List<WeeklyTimeSheet> weeklyTimeSheets = new ArrayList<>();
            Map<Integer, Long> weeklyHoursMap = new HashMap<>();
            int count = 0;

            for (LocalDate weekStart : weekStartDates) {
                LocalTime fixedTime = LocalTime.of(5, 30);
                Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStart, fixedTime));

                WeeklyTimeSheet timeSheet = weeklyTimeSheetRepository.findByEmployeeCodeAndWeekStartDate(
                        employee.getEmployeeCode(), startTs);

                if (timeSheet == null) {
                    throw new TimeSheetException(NOT_FOUND_ERROR, DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES +
                            employee.getEmployeeCode() + "   " + weekStart);
                }

                weeklyTimeSheets.add(timeSheet);
            }

            for (WeeklyTimeSheet tempTimeSheet : weeklyTimeSheets) {
                weeklyHoursMap.put(++count, tempTimeSheet.getTotalWorkingHours());
            }

            ManagerDashboardResponse response = new ManagerDashboardResponse();
            response.setEmployeeCode(employee.getEmployeeCode());
            response.setWeek1(weeklyHoursMap.getOrDefault(1, 0L));
            response.setWeek2(weeklyHoursMap.getOrDefault(2, 0L));
            response.setWeek3(weeklyHoursMap.getOrDefault(3, 0L));
            response.setWeek4(weeklyHoursMap.getOrDefault(4, 0L));
            response.setWeek5(weeklyHoursMap.getOrDefault(5, 0L));

            managerDashboardResponses.add(response);
        }

        return managerDashboardResponses;
    }


}

