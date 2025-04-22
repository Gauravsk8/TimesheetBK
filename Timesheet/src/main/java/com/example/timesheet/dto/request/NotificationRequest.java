package com.example.timesheet.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationRequest {
    private List<String> channels;
    private String recipient;
    private String subject;
    private String message;
    private String phone;
}
