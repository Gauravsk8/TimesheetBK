package com.example.timesheet.service;

import com.example.common.audit.AuditEvent;
import com.example.common.audit.AuditKafkaProducer;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.EmployeeRepository;
import com.example.timesheet.Repository.RoleRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.common.constants.errorCode;
import com.example.timesheet.dto.request.EmployeeRequestDto;
import com.example.timesheet.models.Employee;
import com.example.timesheet.models.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final IdentityServiceClient identityServiceClient;
    private final AuditKafkaProducer auditKafkaProducer;

    @Transactional
    public String createEmployee(EmployeeRequestDto employeeRequestDto, String roleName, String token) {
        try {
            log.info("Initiating employee creation for email: {}", employeeRequestDto.getEmail());

            // 1. Call Identity Service to create Keycloak user
            ResponseEntity<Map<String, String>> identityResponse =
                    identityServiceClient.createKeycloakUser(token, employeeRequestDto, roleName);
            String keycloakUserId = identityResponse.getBody().get("keycloakUserId");
            log.info("Keycloak user created with ID: {}", keycloakUserId);

            // 2. Check for existing employee
            if (employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())) {
                log.warn("Attempt to create employee that already exists: {}", employeeRequestDto.getEmail());
                throw new TimeSheetException(errorCode.CONFLICT_ERROR,
                        String.format(EMPLOYEE_ALREADY_EXISTS, employeeRequestDto.getEmail()));
            }

            // 3. Create employee record
            Role role = roleRepository.findByNameIgnoreCase(roleName)
                    .orElseThrow(() -> {
                        log.error("Role not found: {}", roleName);
                        return new TimeSheetException(
                                errorCode.NOT_FOUND_ERROR,
                                String.format(ROLE_NOT_FOUND, roleName));
                    });

            Employee employee = new Employee();
            employee.setEmail(employeeRequestDto.getEmail());
            employee.setFirstName(employeeRequestDto.getFirstName());
            employee.setLastName(employeeRequestDto.getLastName());
            employee.setPhone(employeeRequestDto.getPhone());
            employee.setEmployeeId(employeeRequestDto.getEmployeeId());
            employee.setKeycloakId(keycloakUserId);
            employee.setTenantId("one");
            employee.setRoles(Collections.singleton(role));
            employee.setEnabled(true);
            employee.setPassword(employeeRequestDto.getPassword());

            Employee savedEmployee = employeeRepository.save(employee);
            log.info("Employee saved successfully with ID: {}", savedEmployee.getId());

            // 4. Send audit event
            AuditEvent auditEvent = new AuditEvent(
                    "TimesheetEmployeeService",
                    employeeRequestDto.getEmail(),
                    "CREATE_EMPLOYEE",
                    Instant.now(),
                    Map.of("employeeId", savedEmployee.getId(), "role", roleName)
            );

            log.info("Sending audit event to Kafka: {}", auditEvent);
            System.out.println(auditKafkaProducer.sendAudit(auditEvent));
            log.info("Audit event sent successfully for employee ID: {}", savedEmployee.getId());

            return String.format(EMPLOYEE_CREATION_SUCCESS, savedEmployee.getId());

        } catch(TimeSheetException e) {
            log.error("TimeSheetException during employee creation: {}", e.getMessage());
            throw e;
        } catch(Exception e) {
            log.error("Unexpected error during employee creation: {}", e.getMessage(), e);
            throw new TimeSheetException(errorCode.INTERNAL_SERVER_ERROR,
                    EMPLOYEE_CREATION_FAILED_LOG + ": " + e.getMessage(), e);
        }
    }
}
