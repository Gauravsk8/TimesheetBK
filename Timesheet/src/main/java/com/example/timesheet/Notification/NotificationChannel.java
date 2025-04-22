package com.example.timesheet.Notification;

import com.example.timesheet.dto.request.NotificationRequest;

public interface NotificationChannel {
    void send(NotificationRequest request);
}
