package com.example.timesheet.Repository;


import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, ProjectEmployeeId> {

    List<ProjectEmployee> findByProject_ProjectCode(String projectCode);

    // Optional: check if a user is assigned to a project

}

