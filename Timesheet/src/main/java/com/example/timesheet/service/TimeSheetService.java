package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.ProjectTimeEntryRepository;
import com.example.timesheet.Repository.WeeklyTimeSheetRepository;
import com.example.timesheet.dto.request.DailyTimeSheetRequest;
import com.example.timesheet.dto.request.ProjectTimeSheetEntryRequest;
import com.example.timesheet.dto.request.WeeklyTimeSheetRequest;
import com.example.timesheet.dto.response.DailyTimeSheetResponse;
import com.example.timesheet.dto.response.ProjectTimeSheetEntryResponse;
import com.example.timesheet.dto.response.WeeklyTimeSheetResponse;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.ProjectTimeEntry;
import com.example.timesheet.models.WeeklyTimeSheet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.example.common.constants.errorCode.NOT_FOUND_ERROR;

@Service
@Transactional
@RequiredArgsConstructor
public class TimeSheetService {

    private final DailyTimeSheetRepository dailyTimeSheetRepository;
    private final ProjectTimeEntryRepository projectTimeEntryRepository;
    private final WeeklyTimeSheetRepository weeklyTimeSheetRepository;
    public String enterOrUpdateDailyTimeSheet(DailyTimeSheetRequest dailyTimeSheetRequest) {

        Long totalHours=0L;
        DailyTimeSheet dailyTimeSheet = dailyTimeSheetRepository.findByDateAndEmployeeCode(dailyTimeSheetRequest.getDate(),dailyTimeSheetRequest.getEmployeeCode());

        if (dailyTimeSheet == null) {
            dailyTimeSheet = new DailyTimeSheet(); // if not found, create new
        }
        else {
            // Delete all existing project entries for the current dailyTimeSheet
            projectTimeEntryRepository.deleteByDailyTimeSheetId(dailyTimeSheet.getId());
        }
        dailyTimeSheet.setDate(dailyTimeSheetRequest.getDate());
        dailyTimeSheet.setIdeal(dailyTimeSheetRequest.getIdeal());
        dailyTimeSheet.setHoliday(dailyTimeSheetRequest.getHoliday());
        dailyTimeSheet.setLeave(dailyTimeSheetRequest.getLeave());
        dailyTimeSheet.setTraining(dailyTimeSheetRequest.getTraining());
        //dailyTimeSheet.setTotalHours(dailyTimeSheetRequest.getTotalHours());
        dailyTimeSheet.setEmployeeId(dailyTimeSheetRequest.getEmployeeId());
        dailyTimeSheet.setEmployeeCode(dailyTimeSheetRequest.getEmployeeCode());
        DailyTimeSheet savedDailyTimeSheet = dailyTimeSheetRepository.save(dailyTimeSheet);
        totalHours+= dailyTimeSheetRequest.getLeave()
                + dailyTimeSheetRequest.getHoliday()
                +dailyTimeSheetRequest.getTraining()
                +dailyTimeSheetRequest.getIdeal()
                ;
        // Create new project entries
        List<ProjectTimeEntry> newEntries = new ArrayList<>();
        for (ProjectTimeSheetEntryRequest e : dailyTimeSheetRequest.getProjectTimeSheetEntryRequests()) {
            ProjectTimeEntry entry = new ProjectTimeEntry();
            entry.setProjectId(e.getProjectId());
            entry.setTotalHoursSpent(e.getTotalHoursSpent());
            entry.setDailyTimeSheet(savedDailyTimeSheet);
            totalHours+=e.getTotalHoursSpent();
            newEntries.add(entry);
        }
        dailyTimeSheet.setTotalHours(totalHours);

        projectTimeEntryRepository.saveAll(newEntries);
        return savedDailyTimeSheet.getId() != null ? "Saved" : "Error saving daily time sheet";
    }

    public String weeklyTimeSheetEntry(WeeklyTimeSheetRequest weeklyTimeSheetRequest){
        System.out.println("Week start date:"+weeklyTimeSheetRequest.getWeekStartDate());
        List<DailyTimeSheet> dailySheets = dailyTimeSheetRepository
                .findByEmployeeCodeAndDateBetween(weeklyTimeSheetRequest.getEmployeeCode(), weeklyTimeSheetRequest.getWeekStartDate(), weeklyTimeSheetRequest.getWeekEndDate());
        if(dailySheets.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,"Daily time sheets not found for this employee between these dates");
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

        if (weeklyTimeSheet.getDailySheets() == null) {
            weeklyTimeSheet.setDailySheets(new ArrayList<>());
        } else {
            weeklyTimeSheet.getDailySheets().clear(); // 🔁 clear old list safely
        }

        for (DailyTimeSheet daily : dailySheets) {
            daily.setWeeklyTimeSheet(weeklyTimeSheet); // link back to weekly
            weeklyTimeSheet.getDailySheets().add(daily); // add one by one
        }


        weeklyTimeSheetRepository.save(weeklyTimeSheet);

        return weeklyTimeSheet.getId()!=null?"Saved":"Error saving weekly time sheet";

    }

    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(String employeeCode, Timestamp weekStartDate,Timestamp weekEndDate) {

        WeeklyTimeSheet weeklyTimeSheet=weeklyTimeSheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode,weekStartDate);
        if(weeklyTimeSheet==null){
            throw new TimeSheetException(NOT_FOUND_ERROR,"Weekly time sheet for employee with employee code:"+employeeCode+" not found");
        }
        WeeklyTimeSheetResponse weeklyTimeSheetResponse=new WeeklyTimeSheetResponse();
        weeklyTimeSheetResponse.setId(weeklyTimeSheet.getId());
        weeklyTimeSheetResponse.setEmployeeId(weeklyTimeSheet.getEmployeeId());
        weeklyTimeSheetResponse.setTotalWorkingHours(weeklyTimeSheet.getTotalWorkingHours());
        weeklyTimeSheetResponse.setTotalIdleHours(weeklyTimeSheetResponse.getTotalIdleHours());

        List<DailyTimeSheetResponse> dailyTimeSheetResponses=new ArrayList<>();
        for(DailyTimeSheet dailyTimeSheet: weeklyTimeSheet.getDailySheets()){
            DailyTimeSheetResponse dailyTimeSheetResponse=new DailyTimeSheetResponse();
            dailyTimeSheetResponse.setEmployeeCode(dailyTimeSheet.getEmployeeCode());
            dailyTimeSheetResponse.setIdeal(dailyTimeSheet.getIdeal());
            dailyTimeSheetResponse.setDate(dailyTimeSheet.getDate());
            dailyTimeSheetResponse.setHoliday(dailyTimeSheet.getHoliday());
            dailyTimeSheetResponse.setLeave(dailyTimeSheet.getLeave());
            dailyTimeSheetResponse.setTraining(dailyTimeSheet.getTraining());
            dailyTimeSheetResponse.setId(dailyTimeSheet.getId());
            dailyTimeSheetResponse.setEmployeeId(dailyTimeSheet.getEmployeeId());
            dailyTimeSheetResponse.setTotalHours(dailyTimeSheet.getTotalHours());

            List<ProjectTimeSheetEntryResponse> projectTimeSheetEntryResponses=new ArrayList<>();
            for(ProjectTimeEntry projectTimeEntry:dailyTimeSheet.getProjectTimeEntries()){
                ProjectTimeSheetEntryResponse projectTimeSheetEntryResponse=new ProjectTimeSheetEntryResponse();
                projectTimeSheetEntryResponse.setId(projectTimeEntry.getId());
                projectTimeSheetEntryResponse.setProjectId(projectTimeEntry.getProjectId());
                projectTimeSheetEntryResponse.setTotalHoursSpent(projectTimeEntry.getTotalHoursSpent());
                projectTimeSheetEntryResponses.add(projectTimeSheetEntryResponse);
            }
            dailyTimeSheetResponse.setProjectTimeSheetEntryResponses(projectTimeSheetEntryResponses);
            dailyTimeSheetResponses.add(dailyTimeSheetResponse);
        }
        weeklyTimeSheetResponse.setDailyTimeSheetResponses(dailyTimeSheetResponses);
        return  weeklyTimeSheetResponse;



    }

    public Long getWeeklyHoursSpent(Long projectId, String employeeCode, Timestamp weekStartDate,Timestamp weekEndDate) {
        List<DailyTimeSheet> dailyTimeSheets=dailyTimeSheetRepository.findByEmployeeCodeAndDateBetween(employeeCode,weekStartDate,weekEndDate);
        if(dailyTimeSheets.isEmpty()){
            throw new TimeSheetException(NOT_FOUND_ERROR,"Daily time sheets not found for this employee between these dates, unable to fetch weekly hours spent");
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
        throw new TimeSheetException(NOT_FOUND_ERROR,"Daily time sheets not found for this employee between these dates, unable to fetch weekly hours spent");
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

}

