package com.example.timesheet.dto.response;

import lombok.Data;

import java.sql.Date;

@Data
public class MonthlyReportEntryDto {
    private String employeeCode;
    private String projectCode;
    private Date workDate;
    private Double hoursSpent;
    private String description;
}
