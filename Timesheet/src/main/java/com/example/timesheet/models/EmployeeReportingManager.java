package com.example.timesheet.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmployeeReportingManager {
    @Id
    private String employeeCode;
    private String managerCode;

}
