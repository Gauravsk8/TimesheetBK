package com.example.timesheet.controller;

import com.example.timesheet.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.config.KeycloakAuthorizationEnforcer;
import com.example.timesheet.models.Employee;
import com.example.timesheet.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final KeycloakAuthorizationEnforcer enforcer;

    @PostMapping("/create")
    @RequiresKeycloakAuthorization(resource = "employee", scope = "testscope")
    public ResponseEntity<?> createEmployee(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody Employee employee,
            @RequestParam String role) {

        String result = employeeService.createEmployee(employee, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}

