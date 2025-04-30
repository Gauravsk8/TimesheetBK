package com.example.IdentityManagementService.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public String loadTemplate(String templateName, Map<String, String> variables) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            String template;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                template = reader.lines().collect(Collectors.joining("\n"));
            }

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return template;

        } catch (Exception e) {
            log.error("Failed to load email template", e);
            throw new RuntimeException("Error loading email template: " + templateName, e);
        }
    }
}
