package com.example.timesheet.service;

import com.example.timesheet.Repository.WeeklyTimesheetRepository;
import com.example.timesheet.dto.request.NotificationRequest;
import com.example.timesheet.models.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Service
public class ReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${kafka.topic.timesheet.remainder}")
    private String remainderTopic;

    @Autowired
    private WeeklyTimesheetRepository weeklyTimeSheetRepository;

    @Scheduled(cron = "0 52 8 * * ?", zone = "Asia/Kolkata")
    // For IST
    public void checkAndSendRemainder() {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int week = today.get(weekFields.weekOfWeekBasedYear());
        int year = today.getYear();

        List<Employee> defaulters = weeklyTimeSheetRepository.findEmployeesWithUnsubmittedTimesheets(year, week);

        if (defaulters.isEmpty()) {
            logger.info("No employees with unsubmitted timesheets for this week.");
        } else {
            logger.info("Found {} employees with unsubmitted timesheets.", defaulters.size());
        }

        for (Employee emp : defaulters) {
            String fullName = emp.getFirstName() + " " + emp.getLastName();
            String emailBody = "Hi " + fullName + ",\n\nThis is a friendly reminder to fill out your timesheet before the weekend.\n\nThanks,\nTimesheet Bot";
            NotificationRequest event = new NotificationRequest();
            event.setPhone(emp.getPhone());
            event.setRecipient(emp.getEmail());
            event.setSubject("Reminder: Fill Out Your Timesheet");
            event.setMessage(emailBody);

            kafkaTemplate.send(remainderTopic, event);
            logger.info("Sent reminder event for employee: {}", emp.getEmail());
        }
    }
}
