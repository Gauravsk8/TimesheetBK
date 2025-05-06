package com.example.timesheet.dto.request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ProjectDto {
    private String projectCode;
    private String title;
    private String description;
    private String owner;
    private Timestamp startDate;
    private Timestamp endDate;
    private Long clientId;
    private String costCenterCode;
    private String managerCode;
    private String allocatedHours;
}

