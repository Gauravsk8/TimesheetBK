package com.example.timesheet.Repository;

import com.example.timesheet.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {

    //boolean existsByProjectCode(String projectCode);

   List<Project> findByprojectManagerCodeIgnoreCase(String projectManagerCode);
   List<Project> findByCostCenter_CostCenterCode(String costCenterCode);


}
