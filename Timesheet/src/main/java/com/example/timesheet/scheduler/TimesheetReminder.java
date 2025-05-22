package com.example.timesheet.scheduler;

import com.example.timesheet.client.IdentityServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimesheetReminder {

    private final IdentityServiceClient identityServiceClient;
    private final ReminderEmailService emailService;

    // Runs every Friday at 10:00 AM

    @Scheduled(cron = "0 45 19 ? * WED")
    public void sendWeeklyTimesheetReminder() {
        try {
            System.out.println("Running weekly timesheet reminder job...");

            ResponseEntity<List<Map<String, String>>> response = identityServiceClient.getAllUsersList();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, String>> users = response.getBody();

                for (Map<String, String> user : users) {
                    String email = user.get("email");
                    String firstName = user.get("firstName");
                    String employeeCode = user.get("employeeCode");

                    if (email != null && !email.isBlank()) {
                        String name = (firstName != null && !firstName.isBlank()) ? firstName : employeeCode;

                        emailService.sendTemplatedEmail(email, Map.of("name", name));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error sending weekly reminder emails: " + e.getMessage());
        }
    }

}
