package com.example.timesheet.dto.request;


import lombok.Data;

@Data
public class CostCenterDto {
    private String code;
    private String name;
    private String description;
    private String managerCode;
}

