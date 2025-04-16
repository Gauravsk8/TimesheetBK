package com.example.timesheet.service;

import com.example.timesheet.Repository.WeeklyTimesheetRepository;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.Employee;
import com.example.timesheet.models.Project;
import com.example.timesheet.models.TimeSheetEntry;
import com.example.timesheet.models.WeeklyTimeSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TimesheetApprovalService {

    private final WeeklyTimesheetRepository weeklyTimeSheetRepository;
    private final MailService mailService;  // To send the email

    public void submitWeeklyTimesheet(Long weeklyTimeSheetId) {
        // Fetch the WeeklyTimeSheet by ID
        WeeklyTimeSheet weeklyTimeSheet = weeklyTimeSheetRepository.findById(weeklyTimeSheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        // Set the status to "SUBMITTED"
        weeklyTimeSheet.setStatus("SUBMITTED");
        weeklyTimeSheetRepository.save(weeklyTimeSheet);

        // Collect managers from the projects in the timesheet
        Set<Employee> managersToNotify = new HashSet<>();

        for (DailyTimeSheet dailyTimeSheet : weeklyTimeSheet.getDailyTimeSheets()) {
            for (TimeSheetEntry entry : dailyTimeSheet.getTimeSheetEntryList()) {
                Project project = entry.getProject();  // Get the project from the entry
                Employee manager = project.getManager();  // Get the manager for that project
                if (manager != null && manager.getEmail() != null) {
                    managersToNotify.add(manager);
                }
            }
        }

        // Get the submitting employee
        Employee employee = weeklyTimeSheet.getEmployee();

        // email to each manager
        for (Employee manager : managersToNotify) {
            String subject = " Approval Needed: Weekly Timesheet Submitted";
            String body = "Hi " + manager.getFirstName() + ",\n\n" +
                    "Your team member " + employee.getName() + " has submitted a weekly timesheet for review.\n\n" +
                    "Please review and approve the timesheet in the portal.\n\n" +
                    "Thanks,\nTimesheet Bot";

            // Send email to the manager
            mailService.sendEmail(manager.getEmail(), subject, body);
        }
    }
}
