package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.dto.reponse.ProjectResponseDto;
import com.example.timesheet.dto.reponse.ProjectWithEmployeesDto;
import com.example.timesheet.dto.request.AssignEmployeesDto;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.request.CostCenterDto;
import com.example.timesheet.dto.request.ProjectDto;
import com.example.timesheet.dto.reponse.ProjectEmployeeDto;
import com.example.timesheet.models.Clients;
import com.example.timesheet.models.CostCenter;
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
    public ResponseEntity<List<Clients>> getAllClients() {
        List<Clients> response = projectManagementService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<Clients> getClientById(@PathVariable Long id) {
        return projectManagementService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<Clients> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            Clients updatedClient = projectManagementService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/cost-centers")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<CostCenter> createCostCenter(@RequestBody CostCenterDto dto) {
        return ResponseEntity.ok(projectManagementService.createCostCenter(dto));
    }

    @GetMapping("/admin/cost-centers")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<CostCenter>> getAllCostCenters() {
        return ResponseEntity.ok(projectManagementService.getAllCostCenters());
    }

    @GetMapping("/admin/cost-centers/{code}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<CostCenter> getCostCenterByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(projectManagementService.getCostCenterByCode(code));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/admin/cost-centers/{code}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<CostCenter> updateCostCenter(@PathVariable String code, @RequestBody CostCenterDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateCostCenter(code, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/createProject")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> createProjects(@RequestBody ProjectDto projectCreateRequest){
        String result=projectManagementService.createProject(projectCreateRequest);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/admin/{code}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> updateProject(@PathVariable String code, @RequestBody ProjectDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateProject(code, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/Project")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<ProjectResponseDto>> getAllProject() {
        return ResponseEntity.ok(projectManagementService.getAllProjects());
    }

    @PostMapping("admin/Project/assign")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> assignEmployees(@RequestBody AssignEmployeesDto dto) {
        return ResponseEntity.ok(projectManagementService.assignEmployeesToProject(dto));
    }

    @GetMapping("/admin/{projectCode}/with-employees")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<ProjectWithEmployeesDto> getProjectWithEmployees(@PathVariable String projectCode) {
        ProjectWithEmployeesDto dto = projectManagementService.getProjectWithEmployees(projectCode);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/admin/{projectCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> deleteProject(@PathVariable String projectCode) {
        projectManagementService.deleteProject(projectCode);
        return ResponseEntity.ok("Project with code " + projectCode + " has been deleted.");
    }


    @DeleteMapping("/admin/Project/{projectCode}/employees/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> removeEmployeeFromProject(
            @PathVariable String projectCode,
            @PathVariable String employeeCode) {
        projectManagementService.removeEmployeeFromProject(projectCode, employeeCode);
        return ResponseEntity.ok("Employee removed from project successfully.");
    }
    @GetMapping("/Project/{projectCode}/employees")
    public ResponseEntity<List<ProjectEmployeeDto>> getAssignedEmployees(@PathVariable String projectCode) {
        List<ProjectEmployeeDto> employees = projectManagementService.getEmployeesByProject(projectCode);
        return ResponseEntity.ok(employees);
    }



}

