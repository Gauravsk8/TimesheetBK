package com.example.timesheet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDto {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String phone;
    private String employeeId;
}
