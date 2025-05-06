package com.example.timesheet.dto.response;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class WeeklySummaryResponse {
    private long hours;
    private Long weeklyTimeSheetId;
    private Timestamp weekStartDate;
    private Timestamp weekEndDate;

    public WeeklySummaryResponse() {
    }

    public WeeklySummaryResponse(long hours, Long weeklyTimeSheetId, Timestamp weekStartDate, Timestamp weekEndDate) {
        this.hours = hours;
        this.weeklyTimeSheetId = weeklyTimeSheetId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
    }
}
