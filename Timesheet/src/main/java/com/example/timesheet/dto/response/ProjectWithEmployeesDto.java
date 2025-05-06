package com.example.timesheet.dto.response;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class ProjectWithEmployeesDto {
    private String projectCode;
    private String title;
    private String description;
    private String owner;
    private Timestamp startDate;
    private Timestamp endDate;
    private String costCenterCode;
    private String clientName;
    private String managerCode;
    private List<ProjectEmployeeDto> employees;
}
