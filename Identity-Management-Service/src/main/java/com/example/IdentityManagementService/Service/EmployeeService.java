package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;
import com.example.IdentityManagementService.dto.request.Response.UserResponseDto;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    UserIdentityDto getUserByEmployeeCodedb(String employeeCode);
    void updateActiveStatus(String employeeCode, boolean isActive);

    UserIdentityDto getUserByKeycloakUserId(String keycloakUserId);
    List<Map<String, String>> getAllUsersList();

    PagedResponse<UserResponseDto> getAllUsers(PageRequestDto pageRequestDto);

    List<UserIdentityDto> getActiveEmployeesUnderManager(String managerCode);

}
