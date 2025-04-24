package com.example.timesheet.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignEmployeesDto {
    private String projectCode;
    private List<EmployeeAssignment> employees;

    @Data
    public static class EmployeeAssignment {
        private String employeeCode;
    }
}

