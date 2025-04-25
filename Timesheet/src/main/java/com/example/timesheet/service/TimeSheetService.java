package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.EmployeeReportingManagerRepository;
import com.example.timesheet.Repository.ProjectTimeEntryRepository;
import com.example.timesheet.Repository.WeeklyTimeSheetRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.response.ShiftDetailsResponse;
import com.example.timesheet.dto.request.*;
import com.example.timesheet.dto.response.DailyTimeSheetResponse;
import com.example.timesheet.dto.response.ProjectTimeSheetEntryResponse;
import com.example.timesheet.dto.response.WeeklyTimeSheetResponse;
import com.example.timesheet.enums.TimeSheetStatus;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.EmployeeReportingManager;
import com.example.timesheet.models.ProjectTimeEntry;
import com.example.timesheet.models.WeeklyTimeSheet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static com.example.common.constants.errorCode.*;
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


    public String weeklyTimeSheetEntry(WeeklyTimeSheetRequest weeklyTimeSheetRequest){
        System.out.println("Week start date:"+weeklyTimeSheetRequest.getWeekStartDate());
        List<DailyTimeSheet> dailySheets = dailyTimeSheetRepository
                .findByEmployeeCodeAndDateBetween(weeklyTimeSheetRequest.getEmployeeCode(), weeklyTimeSheetRequest.getWeekStartDate(), weeklyTimeSheetRequest.getWeekEndDate());
        if(dailySheets.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,DAILY_TIME_SHEETS_NOT_FOUND_FOR_EMPLOYEE_BETWEEN_THESE_DATES);
        }
        System.out.println("Size of daily sheets:"+dailySheets.size());
        System.out.println("Start Date: " + weeklyTimeSheetRequest.getWeekStartDate());
        System.out.println("From DB: " + dailySheets.get(0).getDate());
        // Convert the start date from the request (assuming it's in LocalDateTime format)
        //Date startDate = weeklyTimeSheetRequest.getWeekStartDate();
        //Timestamp timestampStartDate = Timestamp.valueOf(startDate); // Convert LocalDateTime to Timestamp
        WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findByWeekStartDateAndEmployeeCode(weeklyTimeSheetRequest.getWeekStartDate(),weeklyTimeSheetRequest.getEmployeeCode());

        //WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findByWeekStartDate(weeklyTimeSheetRequest.getWeekStartDate());

        if (weeklyTimeSheet == null) {
            weeklyTimeSheet = new WeeklyTimeSheet(); // if not found, create new
        }
        //WeeklyTimeSheet weeklyTimeSheet=new WeeklyTimeSheet();
        weeklyTimeSheet.setEmployeeId(weeklyTimeSheetRequest.getEmployeeId());
        weeklyTimeSheet.setEmployeeCode(weeklyTimeSheetRequest.getEmployeeCode());
        weeklyTimeSheet.setWeekStartDate(weeklyTimeSheetRequest.getWeekStartDate());

        Long totalHours=0L;
        // 🔁 Set the weeklyTimeSheet reference in each DailyTimeSheet
        for (DailyTimeSheet daily : dailySheets) {
            totalHours+=daily.getTotalHours();
            daily.setWeeklyTimeSheet(weeklyTimeSheet);
        }
        weeklyTimeSheet.setTotalWorkingHours(totalHours);
        weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.PENDING_APPROVAL);
        if (weeklyTimeSheet.getDailySheets() == null) {
            weeklyTimeSheet.setDailySheets(new ArrayList<>());
        } else {
            weeklyTimeSheet.getDailySheets().clear(); // 🔁 clear old list safely
        }

        for (DailyTimeSheet daily : dailySheets) {
            daily.setWeeklyTimeSheet(weeklyTimeSheet); // link back to weekly
            weeklyTimeSheet.getDailySheets().add(daily); // add one by one
        }


        WeeklyTimeSheet savedWeeklyTimeSheet=weeklyTimeSheetRepository.save(weeklyTimeSheet);
        if(savedWeeklyTimeSheet.getId()==null){
            throw new TimeSheetException(SAVE_ERROR,ERROR_SAVING_WEEKLY_TIMESHEET);
        }
        return "Saved";

    }

    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(String employeeCode, Timestamp weekStartDate,Timestamp weekEndDate) {

        WeeklyTimeSheet weeklyTimeSheet=weeklyTimeSheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode,weekStartDate);
        if(weeklyTimeSheet==null){
            throw new TimeSheetException(NOT_FOUND_ERROR,WEEKLY_TIME_SHEET_NOT_FOUND);
        }
        WeeklyTimeSheetResponse weeklyTimeSheetResponse=new WeeklyTimeSheetResponse();
        weeklyTimeSheetResponse.setId(weeklyTimeSheet.getId());
        weeklyTimeSheetResponse.setEmployeeId(weeklyTimeSheet.getEmployeeId());
        weeklyTimeSheetResponse.setTotalWorkingHours(weeklyTimeSheet.getTotalWorkingHours());
        weeklyTimeSheetResponse.setTotalIdleHours(weeklyTimeSheetResponse.getTotalIdleHours());

        List<DailyTimeSheetResponse> dailyTimeSheetResponses=new ArrayList<>();
        for(DailyTimeSheet dailyTimeSheet: weeklyTimeSheet.getDailySheets()){
            dailyTimeSheetResponses.add(convertToDailyTimeSheetResponse(dailyTimeSheet));
        }
        weeklyTimeSheetResponse.setDailyTimeSheetResponses(dailyTimeSheetResponses);
        return  weeklyTimeSheetResponse;



    }

    public Long getWeeklyHoursSpent(Long projectId, String employeeCode, Timestamp weekStartDate,Timestamp weekEndDate) {
        List<DailyTimeSheet> dailyTimeSheets=dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(employeeCode,weekStartDate,weekEndDate);
        if(dailyTimeSheets.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES);
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
    }


public Long getWeeklyHoursSpentByType(String employeeCode, String type, Timestamp weekStartDate, Timestamp weekEndDate) {
    List<DailyTimeSheet> dailyTimeSheets = dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(employeeCode, weekStartDate, weekEndDate);
    if(dailyTimeSheets.isEmpty()){
        throw new TimeSheetException(NOT_FOUND_ERROR,DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES);
    }
    long totalHours = 0L;

    for (DailyTimeSheet dailyTimeSheet : dailyTimeSheets) {
        if (type == null || dailyTimeSheet == null) continue;

        switch (type.toLowerCase()) {
            case "holiday":
                totalHours += Optional.ofNullable(dailyTimeSheet.getHoliday()).orElse(0L);
                break;
            case "idle":
                totalHours += Optional.ofNullable(dailyTimeSheet.getIdeal()).orElse(0L);
                break;
            case "leave":
                totalHours += Optional.ofNullable(dailyTimeSheet.getLeave()).orElse(0L);
                break;
            case "training":
                System.out.println("hit training");
                totalHours += Optional.ofNullable(dailyTimeSheet.getTraining()).orElse(0L);
                break;
            default:
                System.out.println("Unknown type: " + type);
                break;
        }
    }

    return totalHours;
}


    public List<UserIdentityDto> getAllEmployees(String managerCode) {
        List<EmployeeReportingManager>  employeeReportingManager =employeeReportingManagerRepository.findByManagerCode(managerCode);
        if(employeeReportingManager.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,EMPLOYEES_NOT_FOUND_UNDER_THIS_MANAGER);
        }
        List<UserIdentityDto> employeeDetails = new ArrayList<>();

        for (EmployeeReportingManager employeeReportingManager1 : employeeReportingManager) {
            String employeeCode = employeeReportingManager1.getEmployeeCode();

            try {
                ResponseEntity<UserIdentityDto> response =
                        identityServiceClient.getUserByemployeeCode(employeeCode);
                if (response.getStatusCode().is2xxSuccessful()) {
                    employeeDetails.add(response.getBody());
                }
            } catch (Exception e) {
                // Optionally log or handle exception
                throw  new TimeSheetException(FAILED_TO_FETCH_DETAILS,ERROR_FETCHING_EMPLOYEE_DETAILS);

            }
        }

        return employeeDetails;
    }

    public String approvedByManager(Long weeklyTimeSheetId,String managerCode) {
        WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
                .orElseThrow(() -> new TimeSheetException(NOT_FOUND_ERROR,WEEKLY_TIME_SHEET_NOT_FOUND));

        weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.APPROVED);
        weeklyTimeSheet.setApprovedBy(managerCode); // Replace with logged-in manager's info

        weeklyTimeSheetRepository.save(weeklyTimeSheet);

        return "Weekly time sheet approved successfully.";
    }

    public String approveWithManagerOverWrite(ApproveWithManagerOverWriteRequest approveWithManagerOverWriteRequest,Long  weeklyTimeSheetId,TimeSheetStatus status) {
        List<DailyTimeSheet> dailyTimeSheets=dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(approveWithManagerOverWriteRequest.getEmployeeCode(),approveWithManagerOverWriteRequest.getWeekStartDate(),approveWithManagerOverWriteRequest.getWeekEndDate());
        if(dailyTimeSheets.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES);
        }
        Long weeklyHoursSpent=0L;

        System.out.println("Daily time sheet in manager overwrite:"+dailyTimeSheets.size());
        for (DailyTimeSheet dailyTimeSheet : dailyTimeSheets) {
            for (DailyTimeSheetRequest dailyTimeSheetRequest : approveWithManagerOverWriteRequest.getDailyTimeSheetRequests()) {
                if (dailyTimeSheet.getDate()
                        .equals(dailyTimeSheetRequest.getDate())) {
                    Long totalHours= dailyTimeSheet.getTotalHours();
                    Long oldHoliday=dailyTimeSheet.getHoliday();
                    Long oldTraining=dailyTimeSheet.getTraining();
                    Long oldIdle=dailyTimeSheet.getIdeal();
                    Long oldLeave=dailyTimeSheet.getLeave();
                    System.out.println("Matched:"+dailyTimeSheet.getDate());
                    if(!dailyTimeSheet.getHoliday().equals(dailyTimeSheetRequest.getHoliday())){
                        totalHours-=oldHoliday;
                        totalHours+=dailyTimeSheetRequest.getHoliday();
                        dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());

                        dailyTimeSheet.setHolidayModifiedByManager(true);
                    }
                    if(!dailyTimeSheet.getTraining().equals(dailyTimeSheetRequest.getTraining())){
                        totalHours-=oldTraining;
                        totalHours+=dailyTimeSheetRequest.getTraining();
                        dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());

                        dailyTimeSheet.setTrainingModifiedByManager(true);
                    }
                    if(!dailyTimeSheet.getIdeal().equals(dailyTimeSheetRequest.getIdeal())){
                        totalHours-=oldIdle;
                        totalHours+=dailyTimeSheetRequest.getIdeal();
                        dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());

                        dailyTimeSheet.setIdealModifiedByManager(true);
                    }
                    if(!dailyTimeSheet.getLeave().equals(dailyTimeSheetRequest.getLeave())){
                        totalHours-=oldLeave;
                        totalHours+=dailyTimeSheetRequest.getLeave();
                        dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());

                        dailyTimeSheet.setLeaveModifiedByManager(true);
                    }


                    for (ProjectTimeEntry projectTimeEntry : dailyTimeSheet.getProjectTimeEntries()) {
                        Long oldProjectHours=projectTimeEntry.getTotalHoursSpent();
                        System.out.println("Old project hours:"+oldProjectHours);
                        for (ProjectTimeSheetEntryRequest projectTimeSheetEntryRequest : dailyTimeSheetRequest.getProjectTimeSheetEntryRequests()) {
                            if (projectTimeEntry.getProjectId().equals(projectTimeSheetEntryRequest.getProjectId())) {
                                if(!projectTimeEntry.getTotalHoursSpent().equals(projectTimeSheetEntryRequest.getTotalHoursSpent())){
                                    totalHours-=oldProjectHours;
                                    totalHours+=projectTimeSheetEntryRequest.getTotalHoursSpent();
                                    System.out.println("Old project hour:"+totalHours);
                                    projectTimeEntry.setTotalHoursSpent(projectTimeSheetEntryRequest.getTotalHoursSpent());
                                    projectTimeEntry.setTotalHoursSpentOnProjectModifiedByManager(true);

                                }

                            }
                        }
                    }
                    System.out.println("Total hours:"+totalHours);
                    dailyTimeSheet.setTotalHours(totalHours);

                }
            }
            weeklyHoursSpent+=dailyTimeSheet.getTotalHours();
            dailyTimeSheetRepository.save(dailyTimeSheet);  // You are saving here which is good
        }
        WeeklyTimeSheet weeklyTimeSheet=weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
                .orElseThrow(()->new TimeSheetException(NOT_FOUND_ERROR,WEEKLY_TIME_SHEET_NOT_FOUND));
        weeklyTimeSheet.setTimeSheetStatus(status);
        weeklyTimeSheet.setTotalWorkingHours(weeklyHoursSpent);
        weeklyTimeSheet.setCommentsByManager(approveWithManagerOverWriteRequest.getCommentsByManager());
        return status==TimeSheetStatus.MANAGER_APPROVED? "Approved and saved manager overwritten changes":"Manager sent back weekly timesheet";
    }

//    public String sendBackWeeklyTimSheetByManager(Long weeklyTimeSheetId,String commentsByManager) {
//        WeeklyTimeSheet weeklyTimeSheet=weeklyTimeSheetRepository.findById(String.valueOf(weeklyTimeSheetId))
//                .orElseThrow(()->new RuntimeException("No weekly time sheets found for that id"));
//        weeklyTimeSheet.setTimeSheetStatus(TimeSheetStatus.OPEN);
//        weeklyTimeSheet.setCommentsByManager(commentsByManager);
//        return "Manager sent back weekly timesheet";
//    }

    public ShiftDetailsResponse shiftDetailsOfEmployee(Long id) {
        ShiftDetailsResponse shiftDetailsResponse=new ShiftDetailsResponse();
        shiftDetailsResponse.setStartDay(DayOfWeek.MONDAY);
        shiftDetailsResponse.setEndDay(DayOfWeek.FRIDAY);
        shiftDetailsResponse.setStartTime(LocalTime.of(10,0));
        shiftDetailsResponse.setEndTime(LocalTime.of(19,0));
        return shiftDetailsResponse;
    }

    private void addDailyTimeSheet(DailyTimeSheet dailyTimeSheet,DailyTimeSheetRequest dailyTimeSheetRequest){
        dailyTimeSheet.setDate(dailyTimeSheetRequest.getDate());
        dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());
        dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());
        dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());
        dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());
        //dailyTimeSheet.setTotalHours(dailyTimeSheetRequest.getTotalHours());
        dailyTimeSheet.setEmployeeId(dailyTimeSheetRequest.getEmployeeId());
        dailyTimeSheet.setEmployeeCode(dailyTimeSheetRequest.getEmployeeCode());
    }

    private void addProjectTimeSheetEntry(ProjectTimeEntry entry,ProjectTimeSheetEntryRequest e,DailyTimeSheet savedDailyTimeSheet){
        entry.setProjectId(e.getProjectId());
        entry.setTotalHoursSpent(e.getTotalHoursSpent());
        entry.setDailyTimeSheet(savedDailyTimeSheet);
    }
    private DailyTimeSheetResponse convertToDailyTimeSheetResponse(DailyTimeSheet dailyTimeSheet) {
        DailyTimeSheetResponse response = new DailyTimeSheetResponse();
        response.setEmployeeCode(dailyTimeSheet.getEmployeeCode());
        response.setIdeal(dailyTimeSheet.getIdeal());
        response.setDate(dailyTimeSheet.getDate());
        response.setHoliday(dailyTimeSheet.getHoliday());
        response.setLeave(dailyTimeSheet.getLeave());
        response.setTraining(dailyTimeSheet.getTraining());
        response.setId(dailyTimeSheet.getId());
        response.setEmployeeId(dailyTimeSheet.getEmployeeId());
        response.setTotalHours(dailyTimeSheet.getTotalHours());

        List<ProjectTimeSheetEntryResponse> projectResponses = new ArrayList<>();
        for (ProjectTimeEntry entry : dailyTimeSheet.getProjectTimeEntries()) {
            ProjectTimeSheetEntryResponse projectResponse = new ProjectTimeSheetEntryResponse();
            projectResponse.setId(entry.getId());
            projectResponse.setProjectId(entry.getProjectId());
            projectResponse.setTotalHoursSpent(entry.getTotalHoursSpent());
            projectResponses.add(projectResponse);
        }
        response.setProjectTimeSheetEntryResponses(projectResponses);
        return response;
    }
    private Long calculateTotalLoggedHours(DailyTimeSheetRequest req) {
        long total = req.getLeave() + req.getHoliday() + req.getTraining() + req.getIdeal();
        for (ProjectTimeSheetEntryRequest projectReq : req.getProjectTimeSheetEntryRequests()) {
            total += projectReq.getTotalHoursSpent();
        }
        return total;
    }


}

