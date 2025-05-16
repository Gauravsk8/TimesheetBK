package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.AssignEmployeesDto;
import com.example.timesheet.dto.request.ProjectDto;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;

    @PostMapping("/project")
    public ResponseEntity<String> createProjects(@RequestBody ProjectDto projectCreateRequest) {
        try {
            String response = projectManagementService.createProject(projectCreateRequest);
            return ResponseEntity.ok(response);
        } catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PutMapping("/project/{projectCode}")
    public ResponseEntity<String> updateProject(@PathVariable String projectCode, @RequestBody ProjectDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateProject(projectCode, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/project")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        return ResponseEntity.ok(projectManagementService.getAllProjects());
    }

    @PostMapping("project/{projectCode}/assign_employee")
    public ResponseEntity<String> assignEmployees(@RequestBody AssignEmployeesDto dto, @PathVariable String projectCode) {
        return ResponseEntity.ok(projectManagementService.assignEmployeesToProject(dto, projectCode));
    }

    @GetMapping("project/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<ProjectWithEmployeesDto> getProjectWithEmployees(@PathVariable String projectCode) {
        return ResponseEntity.ok(projectManagementService.getProjectWithEmployees(projectCode));
    }

    @DeleteMapping("project/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> deleteProject(@PathVariable String projectCode) {
        projectManagementService.deleteProject(projectCode);
        return ResponseEntity.ok("Project with code " + projectCode + " has been deleted.");
    }

    @GetMapping("/project/{projectCode}/employees")
    public ResponseEntity<List<ProjectEmployeeDto>> getAssignedEmployees(@PathVariable String projectCode) {
        return ResponseEntity.ok(projectManagementService.getEmployeesByProject(projectCode));
    }

    @DeleteMapping("/project/{projectCode}/employee/{employeeCode}")
    public ResponseEntity<String> removeEmployeeFromProject(
            @PathVariable String projectCode,
            @PathVariable String employeeCode) {
        projectManagementService.removeEmployeeFromProject(projectCode, employeeCode);
        return ResponseEntity.ok("Employee removed from project successfully.");
    }

    @PutMapping("/project/{projectCode}/employee/{employeeCode}/")
    public ResponseEntity<String> updateEmployeeSDED(
            @PathVariable String projectCode,
            @PathVariable String employeeCode,
            @RequestBody AssignEmployeesDto.EmployeeAssignment dto) {
        return ResponseEntity.ok(projectManagementService.updateEmployee(projectCode, employeeCode, dto));
    }

    @PutMapping("/project/{projectCode}/employee/{employeeCode}")
    public ResponseEntity<String> updateEmployeeStatus(
            @PathVariable String projectCode,
            @PathVariable String employeeCode,
            @RequestParam String newStatus) {
        return ResponseEntity.ok(projectManagementService.updateEmployeeStatus(projectCode, employeeCode, newStatus));
    }

    @GetMapping("/employee/{employeeCode}/projects")
    public ResponseEntity<List<ProjectDto>> getProjectsByEmployee(@PathVariable String employeeCode) {
        return ResponseEntity.ok(projectManagementService.getProjectsByEmployeeCode(employeeCode));
    }

    @GetMapping("/project/{projectCode}/unassigned_employees")
    public ResponseEntity<List<Map<String, String>>> getUnassignedUsers(@PathVariable String projectCode) {
        return ResponseEntity.ok(projectManagementService.getUnassignedUsersForProject(projectCode));
    }

    @GetMapping("/manager/{managerCode}/projects")
    public ResponseEntity<List<ProjectWithEmployeesDto>> getProjectsUnderManager(@PathVariable String managerCode) {
        return ResponseEntity.ok(projectManagementService.getProjectsWithEmployeesUnderManager(managerCode));
    }
}
