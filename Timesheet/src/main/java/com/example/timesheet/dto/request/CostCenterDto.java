package com.example.timesheet.dto.request;


import lombok.Data;

@Data
public class CostCenterDto {
    private String costCenterCode;
    private String name;
    private String description;
    private String costCenterManagerCode;
}

