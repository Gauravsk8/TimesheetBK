package com.example.timesheet.controller;


import com.example.timesheet.dto.response.ManagerDashboardResponse;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController

@RequestMapping("/timesheet")

@RequiredArgsConstructor
public class TimeSheetController {

    private final TimeSheetService timeSheetService;


    @PostMapping("/User/Employee/daily")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")

    public ResponseEntity<String> enterDailyTimeSheet(@RequestBody List<DailyTimeSheetRequest> dailyTimeSheetRequests){
        String result=timeSheetService.enterOrUpdateDailyTimeSheet(dailyTimeSheetRequests);
        return  ResponseEntity.ok().body(result);
    }


    @PostMapping("/User/Employee/weekly")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")

    public ResponseEntity<String> enterWeeklyTimeSheet(@RequestBody WeeklyTimeSheetRequest weeklyTimeSheetRequest){
        String result=timeSheetService.weeklyTimeSheetEntry(weeklyTimeSheetRequest);
        return  ResponseEntity.ok().body(result);
    }


    @GetMapping("/dashboard/manager/{managerCode}")

    public List<ManagerDashboardResponse> getManagerDashboardResponse(@PathVariable("managerCode") String managerCode,
                                                                      @RequestParam String monthYear){
        return timeSheetService.getManagerDashboardResponse(managerCode,monthYear);
    }
    @PreAuthorize("hasAuthority('SCOPE_view_timesheet') or hasAuthority('SCOPE_view_all_timesheets')")
    @GetMapping("/User/Employee/weekly")
    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")


    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(@RequestParam("employeeCode") String employeeCode,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){

        System.out.println("Week start date in controller:"+weekStartDate);
        // Convert to Timestamp only if needed later
        // Manually set to 05:30:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime).format(formatter));

        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime).format(formatter));
        System.out.println("Week start date in controller:"+startTs);
        return timeSheetService.getWeeklyTimeSheetForAnEmployee(employeeCode,startTs,endTs);
    }


    @GetMapping("/Employee/weekly/{id}")
    //@RequiresKeycloakAuthorization(resource = "ReportingManager", scope = "RMscope")
    public WeeklyTimeSheetResponse getWeeklyTimeSheetByWeeklyTimeSheetID(@PathVariable("id") Long id,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime).format(formatter));

        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime).format(formatter));
        return timeSheetService.getWeeklyTimeSheetByWeeklyTimeSheetID(id,startTs,endTs);
    }

    @GetMapping("/User/Employee/weekly/project-hours")

    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
    public Long getWeeklyHoursSpent(@RequestParam("projectCode") String projectCode,
                                    @RequestParam("employeeCode") String employeeCode,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return  timeSheetService.getWeeklyHoursSpent(projectCode ,employeeCode,startTs,endTs);
    }


//    @GetMapping("/weekly/type-hours/{type}")
//    public Long getWeeklyHoursSpent(@PathVariable("type") String type,
//                                    @RequestParam("employeeCode") String employeeCode,
//                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
//                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
//        LocalTime fixedTime = LocalTime.of(5, 30);
//        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
//        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));
//
//        return timeSheetService.getWeeklyHoursSpentByType(employeeCode,type,startTs,endTs);
//    }

//    @GetMapping("/shift-details/{id}")
//    public ShiftDetailsResponse shiftDetailsOfEmployee(@PathVariable Long id){
//        return timeSheetService.shiftDetailsOfEmployee(id);
//    }

//    @GetMapping("/Employee/weekly/type-hours/{type}")
//    @RequiresKeycloakAuthorization(resource = "Employee", scope = "Employeescope")
//    public Long getWeeklyHoursSpent(@PathVariable("type") String type,
//                                    @RequestParam("employeeCode") String employeeCode,
//                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
//                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
//        LocalTime fixedTime = LocalTime.of(5, 30);
//        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
//        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));
//
//        return timeSheetService.getWeeklyHoursSpentByType(employeeCode,type,startTs,endTs);
//    }

//    @GetMapping("/Employee/shift-details/{id}")
//    public ShiftDetailsResponse shiftDetailsOfEmployee(@PathVariable Long id){
//        return timeSheetService.shiftDetailsOfEmployee(id);
//    }


    @GetMapping("/employees")
    public List<UserIdentityDto> getAllEmployees(@RequestParam("managerCode")String managerCode){
        return timeSheetService.getAllEmployees(managerCode);

    }

    @PostMapping("/approve/manager-approve/")
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
    @PostMapping("/approve-sendBack/admin")
    public ResponseEntity<String> approveSendBackByAdmin(@RequestParam("weeklyTimeSheetId") Long weeklyTimeSheetId,
                                                    @RequestParam("adminCode") String adminCode){
        String result=timeSheetService.approveSendBackByAdmin(weeklyTimeSheetId,adminCode);
        return  ResponseEntity.ok().body(result);
    }



}
