package com.example.timesheet.Repository;

import com.example.timesheet.models.Privilege;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PrivilegeRepository extends BaseRepository<Privilege, Long>, PagingAndSortingRepository<Privilege, Long>, QuerydslPredicateExecutor<Privilege> {
}
