package com.example.timesheet.Notification;

import com.example.timesheet.dto.request.NotificationRequest;
import com.example.timesheet.Notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.dispatch(request);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Notification sent via: " + request.getChannels()
        ));
    }
}

