package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    UserIdentityDto getUserByEmployeeCodedb(String employeeCode);
    void updateActiveStatus(String employeeCode, boolean isActive);

    UserIdentityDto getUserByKeycloakUserId(String keycloakUserId);

    List<Map<String, String>> getAllUsers();
}
