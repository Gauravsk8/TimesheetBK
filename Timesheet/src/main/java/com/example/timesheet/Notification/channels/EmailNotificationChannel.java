package com.example.timesheet.Notification.channels;

import com.example.timesheet.Notification.NotificationChannel;
import com.example.timesheet.dto.request.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationChannel implements NotificationChannel {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void send(NotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getRecipient());
        message.setSubject(request.getSubject());
        message.setText(request.getMessage());
        try {
            mailSender.send(message);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
