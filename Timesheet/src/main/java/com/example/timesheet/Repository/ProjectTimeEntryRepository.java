package com.example.timesheet.Repository;

import com.example.timesheet.models.ProjectTimeEntry;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProjectTimeEntryRepository extends PagingAndSortingRepository<ProjectTimeEntry, Long>,  org.springframework.data.jpa.repository.JpaRepository<ProjectTimeEntry, Long>, QuerydslPredicateExecutor<ProjectTimeEntry> {


   void deleteByDailyTimeSheetId(Long id);



    List<ProjectTimeEntry> findByDailyTimeSheetIdAndProjectId(Long id, Long projectId);
}
