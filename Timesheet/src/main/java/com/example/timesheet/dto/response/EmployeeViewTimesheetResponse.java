package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeViewTimesheetResponse {
    private Timestamp weekStartDate;
    private Map<String, Long> projectHours; // projectCode -> hours
    private Long idleHours;
    private Long holidayHours;
    private Long trainingHours;
    private Long leaveHours;
}
