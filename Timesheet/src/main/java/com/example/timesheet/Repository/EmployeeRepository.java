package com.example.timesheet.Repository;

import com.example.timesheet.models.Employee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends BaseRepository<Employee, String>, PagingAndSortingRepository<Employee, String>, QuerydslPredicateExecutor<Employee> {
     <T> T findByEmailAndDeletedIsFalse(String userName, Class<T> type);
     Optional<Employee> findByEmployeeIdAndDeletedIsFalse(String id);
     <T> T findByEmail(String userName, Class<T> type);
     Optional<Employee> findByEmailAndDeletedIsFalse(String email);
    List<Employee> findAllByDeletedIsFalse();
     Employee findByPhoneAndDeletedIsFalse(String phone);
    //Update failed attempts
    List<Employee> findAllByRolesNameAndDeletedIsFalse(String role);
    @Transactional
    @Modifying
    @Query("UPDATE Employee e SET e.failedAttempt = :failedAttempts WHERE e.email = :email")
    void updateFailedAttempts(@Param("failedAttempts") int failedAttempts, @Param("email") String email);
   //lock employee
    @Transactional
    @Modifying
    @Query("UPDATE Employee e SET e.enabled = :enabled WHERE e.email = :email")
    void lockEmployee(@Param("enabled") Boolean enabled, @Param("email") String email);

    // Get all email IDs of employees with a specific role
    @Query("SELECT e.email FROM Employee e JOIN e.roles r WHERE r.name = :role")
    List<String> getEmployeeEmailsByRole(@Param("role") String role);

    boolean existsByEmailAndDeletedIsFalse(String email);




}
