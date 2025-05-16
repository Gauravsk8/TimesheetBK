package com.example.timesheet.Repository;

import com.example.timesheet.models.TimesheetSummary;
import com.example.timesheet.keys.TimesheetSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface TimesheetSummaryRepository extends JpaRepository<TimesheetSummary, TimesheetSummaryId> {

    List<TimesheetSummary> findByIdEmployeeCode(String employeeCode);

    List<TimesheetSummary> findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(
            String employeeCode, Integer year, Integer month);
    Optional<TimesheetSummary>  findByIdEmployeeCodeAndIdWeekStart(String employeeCode, Date weekStart);
}
 