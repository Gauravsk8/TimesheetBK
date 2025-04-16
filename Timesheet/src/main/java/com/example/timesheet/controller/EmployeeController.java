package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.dto.request.EmployeeRequestDto;
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

    @PostMapping("/create")
    @RequiresKeycloakAuthorization(resource = "employee", scope = "testscope")
    public ResponseEntity<String> createEmployee(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody EmployeeRequestDto employee,
            @RequestParam String role) {

        String result = employeeService.createEmployee(employee, role, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


}