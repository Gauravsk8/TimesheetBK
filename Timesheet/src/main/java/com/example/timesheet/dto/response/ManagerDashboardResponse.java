package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {
    private String employeeCode;
    private WeeklySummaryResponse week1;
    private WeeklySummaryResponse week2;
    private WeeklySummaryResponse week3;
    private WeeklySummaryResponse week4;
    private WeeklySummaryResponse week5;

}
