package com.example.timesheet.service;

import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.EmployeeReportingManagerRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.models.EmployeeReportingManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.example.common.constants.errorCode.INTERNAL_SERVER_ERROR;
import static com.example.common.constants.errorMessage.ReportingManagerAssigned;
import static com.example.common.constants.errorMessage.ReportingManagerAssignedFAILED;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeReportingManagerRepository employeeReportingManagerRepository;
    private final IdentityServiceClient identityServiceClient;

    public String addReportingManagerToEmployee(String employeeCode, String managerCode) {
        boolean isReportingManager = checkIfManagerHasRole(managerCode, "ReportingManager");

        if (!isReportingManager) {
            throw new TimeSheetException(errorCode.FORBIDDEN_ERROR, errorMessage.ROLE_NOT_FOUND);
        }

        // Proceed with assigning the reporting manager
        EmployeeReportingManager relation = new EmployeeReportingManager();
        relation.setEmployeeCode(employeeCode);
        relation.setManagerCode(managerCode);

        EmployeeReportingManager savedRelation = employeeReportingManagerRepository.save(relation);
        return savedRelation.getEmployeeCode() != null
                ? ReportingManagerAssigned
                : ReportingManagerAssignedFAILED;
    }

    private boolean checkIfManagerHasRole(String managerCode, String role) {
        try {
            List<String> usersWithRole = identityServiceClient.getUsersByRoles(List.of(role)).getBody();
            return usersWithRole != null && usersWithRole.stream()
                    .anyMatch(user -> user.equalsIgnoreCase(managerCode));
        } catch (Exception e) {
            throw new TimeSheetException(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, e);
        }
    }


    public List<String> getEmployeesUnderReportingManager(String managerCode) {
        List<EmployeeReportingManager> relations = employeeReportingManagerRepository.findByManagerCodeIgnoreCase(managerCode);
        return relations.stream()
                .map(EmployeeReportingManager::getEmployeeCode)
                .toList();
    }
}