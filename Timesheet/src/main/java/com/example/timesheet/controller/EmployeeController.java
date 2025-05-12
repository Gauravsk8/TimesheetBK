package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    @PostMapping("/admin/addRMToEmployee")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> addReportingManagerToEmployee(@RequestParam("employeeCode") String employeeCode,
                                                                @RequestParam("mangerCode") String managerCode){
        String result=employeeService.addReportingManagerToEmployee(employeeCode,managerCode);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/admin/employees/underRM")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<List<String>> getEmployeesUnderRM(@RequestParam("managerCode") String managerCode) {
        List<String> employees = employeeService.getEmployeesUnderReportingManager(managerCode);
        return ResponseEntity.ok().body(employees);
    }

    // Endpoint for managers to see their own employees
    @GetMapping("/Rmanager/employees")
    @RequiresKeycloakAuthorization(resource = "ReportingManager", scope = "RMscope")  // Only Reporting Managers
    public ResponseEntity<List<String>> getEmployeesUnderManager(@RequestParam("managerCode") String managerCode) {
        List<String> employees = employeeService.getEmployeesUnderReportingManager(managerCode);
        return ResponseEntity.ok().body(employees);
    }

    @GetMapping("/admin/manager-name")
    @RequiresKeycloakAuthorization(resource = "Admin", scope = "Adminscope")
    public ResponseEntity<String> getManagerNameByEmployeeCode(@RequestParam("employeeCode") String employeeCode) {
        String managerName = employeeService.getManagerNameByEmployeeCode(employeeCode);
        return ResponseEntity.ok().body(managerName);
    }


}
