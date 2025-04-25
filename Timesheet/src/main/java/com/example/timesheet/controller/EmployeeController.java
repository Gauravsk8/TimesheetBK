package com.example.timesheet.controller;

import com.example.timesheet.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    @PostMapping("/addReportingManagerToEmployee")
    public ResponseEntity<String> addReportingManagerToEmployee(@RequestParam("employeeCode") String employeeCode,
                                                                @RequestParam("mangerCode") String managerCode){
        String result=employeeService.addReportingManagerToEmployee(employeeCode,managerCode);
        return ResponseEntity.ok().body(result);
    }
}
