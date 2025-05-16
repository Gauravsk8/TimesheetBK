package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.dto.request.DailyTimeSheetRequestDto;
import com.example.timesheet.dto.request.ManagerApprovalRequestDto;
import com.example.timesheet.dto.request.TimesheetSummaryDto;
import com.example.timesheet.dto.response.DailyTimeSheetResponseDto;
import com.example.timesheet.dto.response.EmployeeWeeklyTimesheetDto;
import com.example.timesheet.dto.response.TimesheetSummaryResponseDto;
import com.example.timesheet.service.TimesheetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimeSheetController {

    private final TimesheetService timesheetService;

    //Save or update daily entry
    @PostMapping("/save")
    @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:add")
    public ResponseEntity<String> saveDailyEntry(@RequestBody List<DailyTimeSheetRequestDto> dto) {
        String response = timesheetService.saveDailyEntry(dto);
        return ResponseEntity.ok(response);
    }

    //Submit weekly timesheet
    @PostMapping("/submit")
    @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:add")
    public ResponseEntity<String> submitWeeklyTimesheet(@RequestBody TimesheetSummaryDto dto) {
        String response = timesheetService.submitTimesheetSummary(dto);
        return ResponseEntity.ok(response);
    }

    //Manager approve or reject weekly timesheet
    @PostMapping("/manager_approval")
    @RequiresKeycloakAuthorization(resource = "tms:rm", scope = "tms:approve:add")
    public ResponseEntity<String> managerApproval(@RequestBody ManagerApprovalRequestDto dto) {
        String response = timesheetService.approveOrRejectWeekly(dto);
        return ResponseEntity.ok(response);
    }

    //Get timesheet summaries for an employee (dashboard)
    @GetMapping("/summaries/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:get")
    public ResponseEntity<List<TimesheetSummaryResponseDto>> getTimesheetSummaries(@PathVariable String employeeCode) {
        List<TimesheetSummaryResponseDto> summaries = timesheetService.getEmployeeTimesheetSummaries(employeeCode);
        return ResponseEntity.ok(summaries);
    }

    //Get daily entries for employee for a week (weekStart date format yyyy-MM-dd)
    @GetMapping("/{employeeCode}/{weekStart}")
    @RequiresKeycloakAuthorization(resource = "tms:rmemp", scope = "tms:timesheet:get")
    public ResponseEntity<List<DailyTimeSheetResponseDto>> getDailyEntries(
            @PathVariable String employeeCode,
            @PathVariable Date weekStart) {
        List<DailyTimeSheetResponseDto> dailyEntries = timesheetService.getDailyEntries(employeeCode, weekStart);
        return ResponseEntity.ok(dailyEntries);
    }

    // 6. Generate monthly project report (by Project Manager)
    @GetMapping("/project_report/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "tms:pm", scope = "tms:timesheet:project:get")
    public ResponseEntity<List<EmployeeWeeklyTimesheetDto>> getWeeklyTimesheetForProject(
            @PathVariable String projectCode,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        List<EmployeeWeeklyTimesheetDto> report = timesheetService.getWeeklyTimesheetsForProject(projectCode, year, month);
        return ResponseEntity.ok(report);
    }
}
