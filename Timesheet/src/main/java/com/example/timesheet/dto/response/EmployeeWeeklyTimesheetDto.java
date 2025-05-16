package com.example.timesheet.dto.response;

import com.example.timesheet.enums.TimeSheetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeWeeklyTimesheetDto {
    private String employeeCode;
    private Date weekStart;
    private TimeSheetStatus status;
    // getters & setters
}