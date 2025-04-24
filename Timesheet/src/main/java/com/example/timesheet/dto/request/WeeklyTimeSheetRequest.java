package com.example.timesheet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class WeeklyTimeSheetRequest {
    Timestamp weekStartDate;
    Timestamp weekEndDate;
    String employeeCode;
    String employeeId;
}
