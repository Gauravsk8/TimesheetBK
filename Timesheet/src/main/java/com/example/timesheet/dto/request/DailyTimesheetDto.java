package com.example.timesheet.dto.request;


import com.example.timesheet.enums.EntryType;
import lombok.Data;

import java.sql.Date;
import java.util.List;


@Data
public class DailyTimesheetDto{
    private List<DailyTimesheetRequestDto> dailyEntry;
    private String employeeCode;
    private Integer timesheetYear;
    private Integer timesheetMonth;
    private Date weekStart;

}
