package com.example.timesheet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ApproveWithManagerOverWriteRequest {
    Timestamp weekStartDate;
    Timestamp weekEndDate;
    String employeeCode;
    String employeeId;
    private String commentsByManager;
    private List<DailyTimeSheetRequest> dailyTimeSheetRequests=new ArrayList<>();
}
