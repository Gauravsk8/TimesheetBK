package com.example.timesheet.controller;


import com.example.timesheet.service.TimesheetApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timesheets")
@RequiredArgsConstructor
public class TimesheetApprovalController {

    private final TimesheetApprovalService timesheetApprovalService;

    @PostMapping("/submit/{weeklyTimeSheetId}")
    public ResponseEntity<String> submitWeeklyTimesheet(@PathVariable Long weeklyTimeSheetId) {
        try {
            timesheetApprovalService.submitWeeklyTimesheet(weeklyTimeSheetId);
            return ResponseEntity.status(HttpStatus.OK).body("Timesheet submitted successfully for approval.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting the timesheet: " + e.getMessage());
        }
    }
}
