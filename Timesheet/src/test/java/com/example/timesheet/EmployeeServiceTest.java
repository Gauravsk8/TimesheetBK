package com.example.timesheet;

import com.example.timesheet.Repository.EmployeeRepository;
import com.example.timesheet.Repository.RoleRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.constants.errorCode;
import com.example.timesheet.constants.errorMessage;
import com.example.timesheet.dto.request.EmployeeRequestDto;
import com.example.timesheet.exceptions.TimeSheetException;
import com.example.timesheet.models.Employee;
import com.example.timesheet.models.Role;
import com.example.timesheet.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmployeeServiceTest {

    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_USER = "ROLE_USER";

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private IdentityServiceClient identityServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee_Success() {
        // Arrange
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("test@example.com");
        employeeRequestDto.setPassword("secret");
        employeeRequestDto.setFirstName("John");
        employeeRequestDto.setLastName("Doe");
        employeeRequestDto.setEmployeeId("EMP123");
        employeeRequestDto.setPhone("1234567890");

        Role role = new Role();
        role.setId(1L);
        role.setName(ROLE_MANAGER);

        String token = "mock-token";

        // Mock IdentityServiceClient response
        Map<String, String> responseMap = Collections.singletonMap("keycloakUserId", "keycloak-id");
        ResponseEntity<Map<String, String>> identityResponse = ResponseEntity.ok(responseMap);
        when(identityServiceClient.createKeycloakUser(token, employeeRequestDto, ROLE_MANAGER))
                .thenReturn(identityResponse);

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_MANAGER)).thenReturn(Optional.of(role));

        Employee savedEmployee = new Employee();
        savedEmployee.setId(123L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        // Act
        String result = employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER, token);

        // Assert
        assertTrue(result.contains("Employee created successfully with ID: 123"));
    }

    @Test
    void testCreateEmployee_AlreadyExists() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("existing@example.com");

        String token = "mock-token";

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(true);

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER, token));

        assertEquals(errorCode.CONFLICT_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testCreateEmployee_RoleNotFound() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("new@example.com");

        String token = "mock-token";

        Map<String, String> responseMap = Collections.singletonMap("keycloakUserId", "keycloak-id");
        ResponseEntity<Map<String, String>> identityResponse = ResponseEntity.ok(responseMap);
        when(identityServiceClient.createKeycloakUser(token, employeeRequestDto, ROLE_USER))
                .thenReturn(identityResponse);

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_USER)).thenReturn(Optional.empty());

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_USER, token));

        assertEquals(errorCode.NOT_FOUND_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Role"));
    }

    @Test
    void testCreateEmployee_DatabaseSaveFails() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("fail@example.com");
        employeeRequestDto.setPassword("pass");
        employeeRequestDto.setFirstName("First");
        employeeRequestDto.setLastName("Last");
        employeeRequestDto.setEmployeeId("EMP456");
        employeeRequestDto.setPhone("9876543210");

        Role role = new Role();
        role.setId(2L);
        role.setName(ROLE_MANAGER);

        String token = "mock-token";

        Map<String, String> responseMap = Collections.singletonMap("keycloakUserId", "k-id");
        ResponseEntity<Map<String, String>> identityResponse = ResponseEntity.ok(responseMap);
        when(identityServiceClient.createKeycloakUser(token, employeeRequestDto, ROLE_MANAGER))
                .thenReturn(identityResponse);

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_MANAGER)).thenReturn(Optional.of(role));

        // Simulate failure by returning employee with null ID
        Employee failedSave = new Employee();
        when(employeeRepository.save(any(Employee.class))).thenReturn(failedSave);

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER, token));

        assertEquals(errorCode.TIMESHEET_SAVING_DATA_TO_DATABASE_FAILED, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Failed to save"));
    }
}
