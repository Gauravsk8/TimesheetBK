package com.example.timesheet.controller;

import com.example.timesheet.service.TimesheetReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetReportController {

    private final TimesheetReportService timesheetReportService;

    @GetMapping("/report/download")
    public ResponseEntity<String> downloadReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String projectCode) {
        return timesheetReportService.generateReport(year, month, projectCode);
    }
}
