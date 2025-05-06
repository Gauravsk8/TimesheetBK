package com.example.timesheet.dto.reponse;


import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ProjectResponseDto {
    private String projectCode;
    private String title;
    private String description;
    private String owner;
    private Timestamp startDate;
    private Timestamp endDate;
    private String clientName;
    private String costCenterCode;
    private String managerCode;
    private String allocatedHours;
}

