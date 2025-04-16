package com.example.timesheet.scheduler;

import com.example.timesheet.service.TimesheetReminderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimesheetReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(TimesheetReminderScheduler.class);
    private final TimesheetReminderService reminderService;

    @Scheduled(cron = "0 0 10 ? * FRI")
    public void sendWeeklyReminder() {
        log.info("Running scheduled job: Timesheet Reminder");
        reminderService.sendRemindersToAllEmployees();
    }
}
