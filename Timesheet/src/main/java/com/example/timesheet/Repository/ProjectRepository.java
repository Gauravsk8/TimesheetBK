package com.example.timesheet.Repository;

import com.example.timesheet.models.Project;
import com.example.timesheet.models.Role;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProjectRepository extends BaseRepository<Project, Long>, PagingAndSortingRepository<Project, Long>, QuerydslPredicateExecutor<Project> {
}
