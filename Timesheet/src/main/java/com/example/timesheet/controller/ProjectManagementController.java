package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.dto.request.AssignEmployeesDto;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.request.CostCenterDto;
import com.example.timesheet.dto.request.ProjectDto;
import com.example.timesheet.enums.EmployeeStatus;
import com.example.timesheet.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;

    @PostMapping("/admin/clients")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> createClient(@RequestBody ClientDto clientDto) {
        String response = projectManagementService.createClient(clientDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/clients")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        List<ClientResponseDto> response = projectManagementService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable Long id) {
        return projectManagementService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            String updatedClient = projectManagementService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PostMapping("/admin/cost-centers")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> createCostCenter(@RequestBody CostCenterDto dto) {
        return ResponseEntity.ok(projectManagementService.createCostCenter(dto));
    }

    @GetMapping("/admin/cost-centers")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<CostCenterResponseDto>> getAllCostCenters() {
        return ResponseEntity.ok(projectManagementService.getAllCostCenters());
    }

    @GetMapping("/admin/cost-centers/{code}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<CostCenterResponseDto> getCostCenterByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(projectManagementService.getCostCenterByCode(code));
        }  catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PutMapping("/admin/cost-centers/{code}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> updateCostCenter(@PathVariable String code, @RequestBody CostCenterDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateCostCenter(code, dto));
        }  catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PostMapping("/admin/Project")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> createProjects(@RequestBody ProjectDto projectCreateRequest){
        try {
            String result=projectManagementService.createProject(projectCreateRequest);
            return ResponseEntity.ok(result);
        } catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PutMapping("/admin/Project/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> updateProject(@PathVariable String projectCode, @RequestBody ProjectDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateProject(projectCode, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/Project")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<ProjectResponseDto>> getAllProject() {
        return ResponseEntity.ok(projectManagementService.getAllProjects());
    }

    @PostMapping("admin/Project/{projectCode}/assign")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> assignEmployees(@RequestBody AssignEmployeesDto dto,@PathVariable String projectCode) {
        return ResponseEntity.ok(projectManagementService.assignEmployeesToProject(dto, projectCode));
    }

    @GetMapping("/admin/Project/{projectCode}/with-employees")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<ProjectWithEmployeesDto> getProjectWithEmployees(@PathVariable String projectCode) {
        ProjectWithEmployeesDto dto = projectManagementService.getProjectWithEmployees(projectCode);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/admin/Project/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> deleteProject(@PathVariable String projectCode) {
        projectManagementService.deleteProject(projectCode);
        return ResponseEntity.ok("Project with code " + projectCode + " has been deleted.");
    }
    @GetMapping("/Project/{projectCode}/employees")
    public ResponseEntity<List<ProjectEmployeeDto>> getAssignedEmployees(@PathVariable String projectCode) {
        List<ProjectEmployeeDto> employees = projectManagementService.getEmployeesByProject(projectCode);
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping("/admin/Project/{projectCode}/employees/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> removeEmployeeFromProject(
            @PathVariable String projectCode,
            @PathVariable String employeeCode) {
        projectManagementService.removeEmployeeFromProject(projectCode, employeeCode);
        return ResponseEntity.ok("Employee removed from project successfully.");
    }

    @PutMapping("/Project/{projectCode}/employee/{employeeCode}/")
    @RequiresKeycloakAuthorization(resource = "ProjectManager", scope = "PMscope")    public ResponseEntity<String> updateEmployeeSDED(
            @PathVariable String projectCode,
            @PathVariable String employeeCode,
            @RequestBody AssignEmployeesDto.EmployeeAssignment dto) {

        // Call the service to update the status
        String response=projectManagementService.updateEmployee(projectCode, employeeCode, dto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/Project/by-employee/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "ProjectManager", scope = "PMscope")    public ResponseEntity<List<ProjectDto>> getProjectsByEmployee(@PathVariable String employeeCode) {
        List<ProjectDto> projects = projectManagementService.getProjectsByEmployeeCode(employeeCode);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/Project/{projectCode}/employee/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "ProjectManager", scope = "PMscope")    public ResponseEntity<String> updateEmployeeStatus(
            @PathVariable String projectCode,
            @PathVariable String employeeCode,
            @RequestParam String newStatus) {

        String response = projectManagementService.updateEmployeeStatus(projectCode, employeeCode, newStatus);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/Project/under-manager/{managerCode}")
    @RequiresKeycloakAuthorization(resource = "ProjectManager", scope = "PMscope")
    public ResponseEntity<List<ProjectWithEmployeesDto>> getProjectsUnderManager(
            @PathVariable String managerCode) {

        List<ProjectWithEmployeesDto> projects = projectManagementService.getProjectsWithEmployeesUnderManager(managerCode);
        return ResponseEntity.ok(projects);
    }



}

