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

    // Constants
    private static final String TEMPLATE_PATH_PREFIX = "templates/";
    private static final String TEMPLATE_VAR_PREFIX = "{{";
    private static final String TEMPLATE_VAR_SUFFIX = "}}";
    private static final String LINE_SEPARATOR = "\n";

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
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH_PREFIX + templateName);
            String template;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                template = reader.lines().collect(Collectors.joining(LINE_SEPARATOR));
            }

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = TEMPLATE_VAR_PREFIX + entry.getKey() + TEMPLATE_VAR_SUFFIX;
                template = template.replace(placeholder, entry.getValue());
            }

            return template;

        } catch (Exception e) {
            log.error("Failed to load email template: {}", templateName, e);
            throw new RuntimeException("Error loading email template: " + templateName, e);
        }
    }
}
