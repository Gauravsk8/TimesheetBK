package com.example.IdentityManagementService.Service.ServiceImpl;

import com.example.IdentityManagementService.Repository.EmployeeRepository;
import com.example.IdentityManagementService.Service.ReportingManagerService;
import com.example.IdentityManagementService.exceptions.TimesheetException;
import com.example.IdentityManagementService.model.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.common.constants.errorCode.*;
import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingManagerServiceImpl implements ReportingManagerService {

    private final EmployeeRepository employeeRepository;
    private final KeycloakAssignRoleServiceImpl keycloakAssignRoleService;

    @Override
    public String addReportingManagerToEmployee(String employeeCode, String managerCode) {
        boolean isReportingManager = checkIfManagerHasRole(managerCode, "ReportingManager");

        if (!isReportingManager) {
            throw new TimesheetException(FORBIDDEN_ERROR, ROLE_NOT_FOUND);
        }

        Employee employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, USER_NOT_FOUND + employeeCode));

        employee.setManagerCode(managerCode);
        Employee savedEmployee = employeeRepository.save(employee);

        return savedEmployee.getEmployeeCode() != null
                ? ReportingManagerAssigned
                : ReportingManagerAssignedFAILED;
    }

    @Override
    public String getManagerNameByEmployeeCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, USER_NOT_FOUND + employeeCode));

        String managerCode = employee.getManagerCode();

        if (managerCode == null || managerCode.isEmpty()) {
            throw new TimesheetException(NOT_FOUND_ERROR, RM_NOT_ASSIGNED + employeeCode);
        }

        Employee manager = employeeRepository.findByEmployeeCodeAndIsActiveTrue(managerCode)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, RM_NOT_FOUND + managerCode));

        return manager.getFirstName() + " " + manager.getLastName();
    }

    private boolean checkIfManagerHasRole(String managerCode, String role) {
        try {
            Map<String, String> usersWithRole = keycloakAssignRoleService.getUsersByRoles(List.of(role));
            return usersWithRole != null &&
                    usersWithRole.keySet().stream().anyMatch(username -> username.equalsIgnoreCase(managerCode));
        } catch (Exception e) {
            throw new TimesheetException(KEYCLOAK_CONNECTION_ERROR, KEYCLOAK_CONNECTION_ERROR, e);
        }
    }
}
