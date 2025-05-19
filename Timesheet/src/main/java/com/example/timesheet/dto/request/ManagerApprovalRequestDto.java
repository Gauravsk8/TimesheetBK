package com.example.timesheet.dto.request;

import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
public class ManagerApprovalRequestDto {
    private String employeeCode;
    private Integer timesheetYear;
    private Integer timesheetMonth;
    private Date weekStart;
    private boolean approve;
    private String managerCode;
    private String comment;
    private List<DailyTimesheetRequestDto> dailyTimeSheetRequests = new ArrayList<>();
}
