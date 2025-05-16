package com.example.timesheet.dto.request;

import com.example.timesheet.enums.EntryType;
import lombok.Data;

import java.sql.Date;

@Data
public class DailyTimeSheetRequestDto {
    private String employeeCode;
    private Integer timesheetYear;
    private Integer timesheetMonth;
    private Date workDate;
    private String projectCode; // Optional if entryType is LEAVE/TRAINING/etc.
    private EntryType entryType;
    private Double hoursSpent;
    private String description;
}
