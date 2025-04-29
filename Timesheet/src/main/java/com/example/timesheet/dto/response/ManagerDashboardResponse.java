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
    private Long week1;
    private Long week2;
    private Long week3;
    private Long week4;
    private Long week5;

}
