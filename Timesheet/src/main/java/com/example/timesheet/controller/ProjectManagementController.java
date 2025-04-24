package com.example.timesheet.controller;

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
@RequestMapping("/Project")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;

    @PostMapping("/clients")
    public ResponseEntity<String> createClient(@RequestBody ClientDto clientDto) {
        String response = projectManagementService.createClient(clientDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Clients>> getAllClients() {
        List<Clients> response = projectManagementService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<Clients> getClientById(@PathVariable Long id) {
        return projectManagementService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<Clients> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            Clients updatedClient = projectManagementService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cost-centers")
    public ResponseEntity<CostCenter> createCostCenter(@RequestBody CostCenterDto dto) {
        return ResponseEntity.ok(projectManagementService.createCostCenter(dto));
    }

    @GetMapping("/cost-centers")
    public ResponseEntity<List<CostCenter>> getAllCostCenters() {
        return ResponseEntity.ok(projectManagementService.getAllCostCenters());
    }

    @GetMapping("/cost-centers/{code}")
    public ResponseEntity<CostCenter> getCostCenterByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(projectManagementService.getCostCenterByCode(code));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/cost-centers/{code}")
    public ResponseEntity<CostCenter> updateCostCenter(@PathVariable String code, @RequestBody CostCenterDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateCostCenter(code, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createProject")
    public ResponseEntity<String> createProjects(@RequestBody ProjectDto projectCreateRequest){
        String result=projectManagementService.createProject(projectCreateRequest);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{code}")
    public ResponseEntity<String> updateProject(@PathVariable String code, @RequestBody ProjectDto dto) {
        try {
            return ResponseEntity.ok(projectManagementService.updateProject(code, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProject() {
        return ResponseEntity.ok(projectManagementService.getAllProjects());
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignEmployees(@RequestBody AssignEmployeesDto dto) {
        return ResponseEntity.ok(projectManagementService.assignEmployeesToProject(dto));
    }

    @GetMapping("/{projectCode}/employees")
    public ResponseEntity<List<ProjectEmployeeDto>> getAssignedEmployees(@PathVariable String projectCode) {
        List<ProjectEmployeeDto> employees = projectManagementService.getEmployeesByProject(projectCode);
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping("/{projectCode}/employees/{employeeCode}")
    public ResponseEntity<String> removeEmployeeFromProject(
            @PathVariable String projectCode,
            @PathVariable String employeeCode) {
        projectManagementService.removeEmployeeFromProject(projectCode, employeeCode);
        return ResponseEntity.ok("Employee removed from project successfully.");
    }

    @GetMapping("/{projectCode}/with-employees")
    public ResponseEntity<ProjectWithEmployeesDto> getProjectWithEmployees(@PathVariable String projectCode) {
        ProjectWithEmployeesDto dto = projectManagementService.getProjectWithEmployees(projectCode);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{projectCode}")
    public ResponseEntity<String> deleteProject(@PathVariable String projectCode) {
        projectManagementService.deleteProject(projectCode);
        return ResponseEntity.ok("Project with code " + projectCode + " has been deleted.");
    }

}

