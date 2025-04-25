package com.example.timesheet.service;

import com.example.timesheet.Repository.EmployeeReportingManagerRepository;
import com.example.timesheet.models.EmployeeReportingManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeReportingManagerRepository employeeReportingManagerRepository;
    public String addReportingManagerToEmployee(String employeeCode, String managerCode) {

        EmployeeReportingManager relation = new EmployeeReportingManager();
        relation.setEmployeeCode(employeeCode);
        relation.setManagerCode(managerCode);

        EmployeeReportingManager employeeReportingManager=employeeReportingManagerRepository.save(relation);
        return employeeReportingManager.getEmployeeCode()!=null?"Reporting manager is assigned to employee":"Failed to assign reporting manager to employee";
    }
}
