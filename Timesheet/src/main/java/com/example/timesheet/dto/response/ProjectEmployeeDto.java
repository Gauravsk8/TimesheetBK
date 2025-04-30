package com.example.timesheet.dto.response;


import com.example.timesheet.enums.EmployeeStatus;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class ProjectEmployeeDto {
    private String employeeCode;
    private String firstName;
    private String lastName;
    private Timestamp startDate;  // New
    private Timestamp endDate;    // New
    private EmployeeStatus status;
}

