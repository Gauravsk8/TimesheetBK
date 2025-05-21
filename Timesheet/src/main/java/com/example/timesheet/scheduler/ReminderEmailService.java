package com.example.timesheet.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReminderEmailService {

    private final JavaMailSender javaMailSender;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ReminderEmailService(JavaMailSender javaMailSender, ResourceLoader resourceLoader) {
        this.javaMailSender = javaMailSender;
        this.resourceLoader = resourceLoader;
    }

    public void sendTemplatedEmail(String to, Map<String, String> variables) {
        try {
            Resource resource = resourceLoader.getResource("classpath:email-templates/weekly_reminder_template.txt");
            String template = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Extract subject and body
            String[] parts = template.split("\n\n", 2);
            String subjectLine = parts[0].replace("Subject: ", "");
            String bodyTemplate = parts.length > 1 ? parts[1] : "";

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                bodyTemplate = bodyTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
                subjectLine = subjectLine.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subjectLine);
            message.setText(bodyTemplate);

            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending templated email: " + e.getMessage());
        }
    }
}
