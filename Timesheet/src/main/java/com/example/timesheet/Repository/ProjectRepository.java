package com.example.timesheet.Repository;

import com.example.timesheet.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {

    boolean existsByProjectCode(String projectCode);
}
