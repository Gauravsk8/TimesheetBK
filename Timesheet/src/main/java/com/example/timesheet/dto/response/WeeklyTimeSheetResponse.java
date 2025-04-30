package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class WeeklyTimeSheetResponse {

    protected Long id;
    private String employeeId;
    private String employeeCode;

    private List<DailyTimeSheetResponse> dailyTimeSheetResponses = new ArrayList<>();

    private Long totalWorkingHours; // Computed field: Sum of project + training hours
    private Long totalIdleHours;    // Sum of idle + leave + holiday

    private Map<Long,Long> projectHours;
    private Map<String,Long> hoursMap;


}
