package com.example.timesheet.Repository;


import com.example.timesheet.models.Employee;
import com.example.timesheet.models.WeeklyTimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeeklyTimesheetRepository extends JpaRepository<WeeklyTimeSheet, Long> {

    @Query("""
    SELECT wt.employee FROM WeeklyTimeSheet wt
    WHERE EXTRACT(YEAR FROM wt.createdAt) = :year
    AND EXTRACT(WEEK FROM wt.createdAt) = :week
    AND wt.status <> 'SUBMITTED'
""")
    List<Employee> findEmployeesWithUnsubmittedTimesheets(@Param("year") int year, @Param("week") int week);

}

