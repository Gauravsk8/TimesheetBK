package com.example.timesheet.Repository;


import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, ProjectEmployeeId> {

    List<ProjectEmployee> findByProject_ProjectCodeIgnoreCase(String projectCode);
    List<ProjectEmployee> findByIdEmployeeCodeIgnoreCase(String employeeCode);


    // Optional: check if a user is assigned to a project

}

