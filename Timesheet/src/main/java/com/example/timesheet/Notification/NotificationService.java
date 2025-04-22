package com.example.timesheet.Notification;

import com.example.timesheet.dto.request.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final Map<String, NotificationChannel> channelMap;

    @Autowired
    public NotificationService(List<NotificationChannel> channels) {
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(
                        c -> c.getClass().getSimpleName().replace("NotificationChannel", "").toUpperCase(),
                        c -> c
                ));
    }

    public void dispatch(NotificationRequest request) {
        for (String channel : request.getChannels()) {
            NotificationChannel notificationChannel = channelMap.get(channel.toUpperCase());
            if (notificationChannel != null) {
                notificationChannel.send(request);
            }
        }
    }
}
