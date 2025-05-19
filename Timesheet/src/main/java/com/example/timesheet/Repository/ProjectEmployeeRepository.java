package com.example.timesheet.Repository;


import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, ProjectEmployeeId> {

    List<ProjectEmployee> findByIdEmployeeCodeIgnoreCaseAndIsActiveTrue(String employeeCode);

    List<ProjectEmployee> findByIdProjectCode(String projectCode);

    List<ProjectEmployee> findByProject_ProjectCodeIgnoreCaseAndIsActiveTrue(String projectCode);

    boolean existsByIdAndIsActiveTrue(ProjectEmployeeId id);

    Optional<ProjectEmployee> findByIdAndIsActiveTrue(ProjectEmployeeId id); // FIXED HERE

    List<ProjectEmployee> findByProject_ProjectCode(String projectCode);
}

