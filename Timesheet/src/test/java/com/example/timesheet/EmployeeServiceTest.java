package com.example.timesheet;

import com.example.timesheet.Repository.EmployeeRepository;
import com.example.timesheet.Repository.RoleRepository;
import com.example.timesheet.constants.errorCode;
import com.example.timesheet.dto.request.EmployeeRequestDto;
import com.example.timesheet.exceptions.TimeSheetException;
import com.example.timesheet.models.Employee;
import com.example.timesheet.models.Role;
import com.example.timesheet.service.EmployeeService;
import com.example.timesheet.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

 class EmployeeServiceTest {
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_USER = "USER";

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private KeycloakService keycloakService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee_Success() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("test@example.com");
        employeeRequestDto.setPassword("secret");
        employeeRequestDto.setFirstName("John");
        employeeRequestDto.setLastName("Doe");

        Role role = new Role();
        role.setId(1L);
        role.setName(ROLE_MANAGER);

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_MANAGER)).thenReturn(Optional.of(role));
        when(keycloakService.createUserWithRole(employeeRequestDto, ROLE_MANAGER)).thenReturn("keycloak-id");

        Employee savedEmployee = new Employee();
        savedEmployee.setId(123L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        String result = employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER);
        assertTrue(result.contains("Employee created successfully with ID: 123"));
    }

    @Test
    void testCreateEmployee_AlreadyExists() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("existing@example.com");

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(true);

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER));

        assertEquals(errorCode.CONFLICT_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testCreateEmployee_RoleNotFound() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("new@example.com");

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_USER)).thenReturn(Optional.empty());

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_USER));

        assertEquals(errorCode.NOT_FOUND_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testCreateEmployee_DatabaseSaveFails() {
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setEmail("fail@example.com");
        employeeRequestDto.setPassword("pass");
        employeeRequestDto.setFirstName("First");
        employeeRequestDto.setLastName("Last");

        Role role = new Role();
        role.setName(ROLE_MANAGER);

        when(employeeRepository.existsByEmailAndDeletedIsFalse(employeeRequestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase(ROLE_MANAGER)).thenReturn(Optional.of(role));
        when(keycloakService.createUserWithRole(employeeRequestDto, ROLE_MANAGER)).thenReturn("k-id");

        // Simulate save failure by returning employee with null ID
        Employee emptySaved = new Employee();
        when(employeeRepository.save(any(Employee.class))).thenReturn(emptySaved);

        TimeSheetException ex = assertThrows(TimeSheetException.class,
                () -> employeeService.createEmployee(employeeRequestDto, ROLE_MANAGER));

        assertEquals(errorCode.TIMESHEET_SAVING_DATA_TO_DATABASE_FAILED, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Failed to save"));
    }
}
