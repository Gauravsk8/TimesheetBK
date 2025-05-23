package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.dto.request.DailyTimesheetDto;
import com.example.timesheet.dto.request.ManagerApprovalRequestDto;
import com.example.timesheet.dto.request.TimesheetSummaryDto;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.dto.response.EmployeeDashboard.EmployeeDashboardDto;
import com.example.timesheet.dto.response.ManagerDashboard.ManagerDashboardDto;
import com.example.timesheet.dto.response.ProjectManagerDashboard.ProjectManagerDashboardDTO;
import com.example.timesheet.service.TimesheetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/tms")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;

    //Save or update daily entry
    @PostMapping("/timesheet/daily")
    @RequiresKeycloakAuthorization(resource = "tms:employee", scope = "tms:timesheet:add")
    public ResponseEntity<String> saveDailyEntry(@RequestBody DailyTimesheetDto dto) {
        String response = timesheetService.saveDailyEntry(dto);
        return ResponseEntity.ok(response);
    }

    //Submit weekly timesheet
    @PostMapping("/timesheet/weekly")
    @RequiresKeycloakAuthorization(resource = "tms:employee", scope = "tms:timesheet:add")
    public ResponseEntity<String> submitWeeklyTimesheet(@RequestBody TimesheetSummaryDto dto) {
        String response = timesheetService.submitTimesheetSummary(dto);
        return ResponseEntity.ok(response);
    }

    //Manager approve or reject weekly timesheet
    @PostMapping("/timesheet/manager")
    @RequiresKeycloakAuthorization(resource = "tms:rm", scope = "tms:approve:add")
    public ResponseEntity<List<String>> managerApproval(@RequestBody List<ManagerApprovalRequestDto> dtoList) {
        List<String> responses = dtoList.stream()
                .map(timesheetService::approveOrRejectWeekly)
                .toList();
        return ResponseEntity.ok(responses);
    }


    //view timesheet
    @GetMapping("/timesheet/{employeeCode}")
    //  @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:get")
    public ResponseEntity<List<TimesheetMatrixRowResponseDto>> getTimesheet(@PathVariable String employeeCode,
                                                                                     @RequestParam Integer year, @RequestParam Integer month) {
        List<TimesheetMatrixRowResponseDto> summaries = timesheetService.getEmployeeTimesheet(employeeCode, year, month);
        return ResponseEntity.ok(summaries);
    }


    //Get daily entries for week from startdate(weekStart date format yyyy-MM-dd)
    @GetMapping("/timesheet/{employeeCode}/{weekStart}")
    @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:get")
    public ResponseEntity<DailyTimesheetResponseWithStatus> getDailyEntries(
            @PathVariable String employeeCode,
            @PathVariable Date weekStart) {
        DailyTimesheetResponseWithStatus dailyEntries = timesheetService.getDailyEntries(employeeCode, weekStart);
        return ResponseEntity.ok(dailyEntries);
    }




    //ManagerDashBoard
    @GetMapping("/manager-dashboard/{managerCode}")
    @RequiresKeycloakAuthorization(resource = "tms:rm", scope = "tms:dashboard:get")
    public ResponseEntity<ManagerDashboardDto> getManagerDashboard(
            @PathVariable String managerCode,
            @RequestParam int year,
            @RequestParam int month) {

        ManagerDashboardDto dashboard = timesheetService.getEmployeesTimesheetUnderManager(managerCode, year, month);
        return ResponseEntity.ok(dashboard);
    }


    //Employee Dashboard
    @GetMapping("/employee-dashboard/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "tms:employee", scope = "tms:dashboard:get")
    public ResponseEntity<EmployeeDashboardDto> getEmployeeDashboard(
            @PathVariable String employeeCode,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(timesheetService.getEmployeeDashboard(employeeCode, year, month));
    }

    @GetMapping("/project-manager-dashboard/{projectManagerCode}")
    @RequiresKeycloakAuthorization(resource = "tms:pm", scope = "tms:dashboard:get")
    public ProjectManagerDashboardDTO getDashboard(@PathVariable String projectManagerCode) {
        return timesheetService.getPmDashboard(projectManagerCode);
    }




}
