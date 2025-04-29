package com.example.IdentityManagementService.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleUpdateRequestDto {
    private String employeeCode;
    private List<String> rolesToAssign;
    private List<String> rolesToRemove;
}
