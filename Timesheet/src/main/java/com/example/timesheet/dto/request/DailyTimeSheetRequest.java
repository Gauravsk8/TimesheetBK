package com.example.timesheet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class DailyTimeSheetRequest {

    private String employeeId;
    private String employeeCode;
    private Timestamp date;
    //private WeeklyTimeSheet weeklyTimeSheet;
    private List<ProjectTimeSheetEntryRequest> projectTimeSheetEntryRequests;

    private Long holiday;
    private Long leave;
    private Long ideal;
    private Long training;
}
