package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.service.TimesheetReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetReportController {

    private final TimesheetReportService timesheetReportService;
    @GetMapping("/report/download")
    @RequiresKeycloakAuthorization(resource = "manager:com", scope = "com:manager:get")
    public ResponseEntity<String> downloadReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return timesheetReportService.generateReport(year, month, projectCode, startDate, endDate);
    }


}
