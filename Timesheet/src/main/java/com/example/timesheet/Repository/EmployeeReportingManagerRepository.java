package com.example.timesheet.Repository;

import com.example.timesheet.models.EmployeeReportingManager;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EmployeeReportingManagerRepository extends PagingAndSortingRepository<EmployeeReportingManager, String>,  org.springframework.data.jpa.repository.JpaRepository<EmployeeReportingManager, String>, QuerydslPredicateExecutor<EmployeeReportingManager> {
    List<EmployeeReportingManager> findByManagerCode(String managerCode);
    List<EmployeeReportingManager> findByManagerCodeIgnoreCase(String managerCode);



}
