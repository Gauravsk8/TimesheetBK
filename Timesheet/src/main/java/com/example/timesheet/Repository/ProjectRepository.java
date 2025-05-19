package com.example.timesheet.Repository;

import com.example.timesheet.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByProjectManagerCodeIgnoreCaseAndIsActiveTrue(String projectManagerCode);

    List<Project> findByCostCenter_CostCenterCodeIgnoreCaseAndIsActiveTrue(String costCenterCode);

    // ✅ Fixed method
    List<Project> findByIsActiveTrue();

    Optional<Project> findByProjectCodeAndIsActiveTrue(String projectCode);
}

