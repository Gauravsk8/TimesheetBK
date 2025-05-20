package com.example.timesheet.Repository;

import com.example.timesheet.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {

    List<Project> findByProjectManagerCodeIgnoreCaseAndIsActiveTrue(String projectManagerCode);

    List<Project> findByCostCenter_CostCenterCodeIgnoreCaseAndIsActiveTrue(String costCenterCode);

    // ✅ Fixed method
    List<Project> findByIsActiveTrue();

    Optional<Project> findByProjectCodeAndIsActiveTrue(String projectCode);
}

