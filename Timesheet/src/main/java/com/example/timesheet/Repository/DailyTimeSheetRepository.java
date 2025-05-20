// DailyTimeSheetRepository.java
package com.example.timesheet.Repository;

import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.keys.DailyTimeSheetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface DailyTimeSheetRepository extends JpaRepository<DailyTimeSheet, DailyTimeSheetId> {

    List<DailyTimeSheet> findByIdEmployeeCodeAndIdWorkDateBetween(String employeeCode, Date startDate, Date endDate);

    List<DailyTimeSheet> findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(String employeeCode, Integer year, Integer month);

    @Query("SELECT SUM(d.hoursSpent) FROM DailyTimeSheet d WHERE d.id.employeeCode = :employeeCode AND d.projectCode = :projectCode AND d.id.workDate BETWEEN :weekStart AND :weekEnd")
    Double sumHoursSpentByEmployeeProjectAndWeek(@Param("employeeCode") String employeeCode,
                                                 @Param("projectCode") String projectCode,
                                                 @Param("weekStart") Date weekStart,
                                                 @Param("weekEnd") Date weekEnd);


}
