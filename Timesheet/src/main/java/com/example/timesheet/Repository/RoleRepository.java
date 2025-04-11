package com.example.timesheet.Repository;

import com.example.timesheet.models.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Long>, PagingAndSortingRepository<Role, Long>, QuerydslPredicateExecutor<Role> {
    Optional<Role> findByName(String roleName);
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    Optional<Role> findByNameIgnoreCase(@Param("name") String name);

    List<Role> findAll();
}
