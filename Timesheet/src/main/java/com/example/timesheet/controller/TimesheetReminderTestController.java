package com.example.timesheet.controller;

import com.example.timesheet.scheduler.TimesheetReminderScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class TimesheetReminderTestController {

    private final TimesheetReminderScheduler reminderScheduler;
    @PostMapping("/timesheet-reminder")
    public String sendRemindersManually() {
        reminderScheduler.sendWeeklyReminder();
        return "Timesheet reminder emails triggered manually!";
    }
}
