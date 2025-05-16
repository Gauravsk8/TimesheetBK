package com.example.IdentityManagementService.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRMRequest {
    private String employeeCode;
    private String managerCode;
}
