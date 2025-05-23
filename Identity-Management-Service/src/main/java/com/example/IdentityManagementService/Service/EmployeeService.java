package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.dto.request.Response.UserResponseDto;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.common.dto.FilterRequest;
import com.example.common.dto.SortRequest;
import com.example.common.dto.response.PagedResponse;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    UserIdentityDto getUserByEmployeeCodedb(String employeeCode);
    void updateActiveStatus(String employeeCode, boolean isActive);

    UserIdentityDto getUserByKeycloakUserId(String keycloakUserId);


     PagedResponse<UserResponseDto> getAllUsers(
            int offset,
            int limit,
            List<FilterRequest> filters,
            List<SortRequest> sorts);

    List<Map<String, String>> getAllUsersList();
    List<UserIdentityDto> getActiveEmployeesUnderManager(String managerCode);

}
