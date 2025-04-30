package com.example.timesheet.dto.request;

import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
public class AssignEmployeesDto {
    private List<EmployeeAssignment> employees;

    @Data
    public static class EmployeeAssignment {
        private String employeeCode;
        private Timestamp startDate;  // New
        private Timestamp endDate;
    }
}

