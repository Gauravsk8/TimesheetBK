package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.dto.response.ShiftDetailsResponse;
import com.example.timesheet.dto.request.ApproveWithManagerOverWriteRequest;
import com.example.timesheet.dto.request.DailyTimeSheetRequest;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.dto.request.WeeklyTimeSheetRequest;
import com.example.timesheet.dto.response.WeeklyTimeSheetResponse;
import com.example.timesheet.enums.TimeSheetStatus;
import com.example.timesheet.service.TimeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;

@RestController("/timesheet")
@RequiredArgsConstructor
public class TimeSheetController {

    private final TimeSheetService timeSheetService;
    @PostMapping("/Employee/daily")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public ResponseEntity<String> enterDailyTimeSheet(@RequestBody List<DailyTimeSheetRequest> dailyTimeSheetRequests){
        String result=timeSheetService.enterOrUpdateDailyTimeSheet(dailyTimeSheetRequests);
        return  ResponseEntity.ok().body(result);
    }

    @PostMapping("/Employee/weekly")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public ResponseEntity<String> enterWeeklyTimeSheet(@RequestBody WeeklyTimeSheetRequest weeklyTimeSheetRequest){
        String result=timeSheetService.weeklyTimeSheetEntry(weeklyTimeSheetRequest);
        return  ResponseEntity.ok().body(result);
    }

    @GetMapping("/Employee/weekly")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(@RequestParam("employeeCode") String employeeCode,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        System.out.println("Week start date in controller:"+weekStartDate);
        // Convert to Timestamp only if needed later
        // Manually set to 05:30:00
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return timeSheetService.getWeeklyTimeSheetForAnEmployee(employeeCode,startTs,endTs);
    }

    @GetMapping("/Employee/weekly/project-hours")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public Long getWeeklyHoursSpent(@RequestParam("projectId") Long projectId,
                                    @RequestParam("employeeCode") String employeeCode,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return  timeSheetService.getWeeklyHoursSpent(projectId,employeeCode,startTs,endTs);
    }

    @GetMapping("/Employee/weekly/type-hours/{type}")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public Long getWeeklyHoursSpent(@PathVariable("type") String type,
                                    @RequestParam("employeeCode") String employeeCode,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return timeSheetService.getWeeklyHoursSpentByType(employeeCode,type,startTs,endTs);
    }

    @GetMapping("/Employee/shift-details/{id}")
    public ShiftDetailsResponse shiftDetailsOfEmployee(@PathVariable Long id){
        return timeSheetService.shiftDetailsOfEmployee(id);
    }

    @GetMapping("/employees")
    public List<UserIdentityDto> getAllEmployees(@RequestParam("managerCode")String managerCode){
        return timeSheetService.getAllEmployees(managerCode);

    }

    @PostMapping("/approve/manager-approve")
    public ResponseEntity<String> approvedByManager(@RequestParam("weeklyTimeSheetId") Long weeklyTimeSheetId,
                                                    @RequestParam("managerCode") String managerCode){
        String result=timeSheetService.approvedByManager(weeklyTimeSheetId,managerCode);
        return  ResponseEntity.ok().body(result);
    }

    @PostMapping("/manager-review")
    public ResponseEntity<String> approveWithManagerOverWrite(@RequestBody ApproveWithManagerOverWriteRequest approveWithManagerOverWriteRequest,
                                                              @RequestParam("weeklyTimeSheetId") Long weeklyTimeSheetId ,
                                                              @RequestParam("status")TimeSheetStatus status){
        String result=timeSheetService.approveWithManagerOverWrite(approveWithManagerOverWriteRequest,weeklyTimeSheetId,status);
        return  ResponseEntity.ok().body(result);
    }


}
