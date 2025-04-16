package com.example.timesheet.service;

import com.example.timesheet.models.Employee;
import com.example.timesheet.Repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetReminderService {

    private final MailService mailService;
    private final EmployeeRepository employeeRepository;

    public void sendRemindersToAllEmployees() {
        List<Employee> allEmployees = employeeRepository.findAll();

        for (Employee employee : allEmployees) {
            if (employee.getEmail() != null && Boolean.TRUE.equals(employee.getEnabled())) {
                String fullName = employee.getFirstName() + " " + employee.getLastName();
                String emailBody = "Hi " + fullName + ",\n\nThis is a friendly reminder to fill out your timesheet before the weekend.\n\nThanks,\nTimesheet Bot";

                mailService.sendEmail(
                        employee.getEmail(),
                        "Reminder: Fill Out Your Timesheet",
                        emailBody
                );
            }
        }

        log.info("Timesheet reminders sent to {} employees.", allEmployees.size());
    }
}
