package com.example.timesheet.dto.request;

import lombok.Data;

import java.sql.Date;

@Data
public class TimesheetSummaryDto {
    private String employeeCode;
    private Integer timesheetYear;
    private Integer timesheetMonth;
    private Date weekStart;
}
