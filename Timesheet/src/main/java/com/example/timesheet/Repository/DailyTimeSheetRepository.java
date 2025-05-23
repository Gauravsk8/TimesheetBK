// DailyTimeSheetRepository.java
package com.example.timesheet.Repository;

import com.example.timesheet.enums.EntryType;
import com.example.timesheet.models.DailyTimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DailyTimeSheetRepository extends JpaRepository<DailyTimeSheet, Long> {

    List<DailyTimeSheet> findByEmployeeCodeAndWorkDateBetween(String employeeCode, Date start, Date end);
    List<DailyTimeSheet> findByProjectCodeIn(List<String> projectCodes);


    List<DailyTimeSheet> findByEmployeeCodeAndTimesheetYearAndTimesheetMonth(String employeeCode, Integer year, Integer month);

    @Query("""
    SELECT SUM(d.hoursSpent)
    FROM DailyTimeSheet d
    WHERE d.employeeCode = :employeeCode
      AND d.projectCode = :projectCode
      AND d.workDate BETWEEN :startDate AND :endDate
""")
    Double sumHoursSpentByEmployeeProjectAndWeek(
            @Param("employeeCode") String employeeCode,
            @Param("projectCode") String projectCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );


    Optional<DailyTimeSheet> findByEmployeeCodeAndTimesheetYearAndTimesheetMonthAndWorkDateAndEntryTypeAndProjectCode(
            String employeeCode,
            Integer timesheetYear,
            Integer timesheetMonth,
            Date workDate,
            EntryType entryType,
            String projectCode
    );

    List<DailyTimeSheet> findByTimesheetYearAndTimesheetMonth(int year, int month);
    List<DailyTimeSheet> findByTimesheetYearAndTimesheetMonthAndProjectCode(int year, int month, String projectCode);

    List<DailyTimeSheet> findByTimesheetYearAndTimesheetMonthAndEmployeeCodeInAndEntryTypeIn(
            int year,
            int month,
            List<String> employeeCodes,
            List<EntryType> entryTypes);
    List<DailyTimeSheet> findByWorkDateBetweenAndProjectCode(Date startDate, Date endDate, String projectCode);

    List<DailyTimeSheet> findByWorkDateBetween(Date startDate, Date endDate);

    List<DailyTimeSheet> findByWorkDateBetweenAndEmployeeCodeInAndEntryTypeIn(
            Date startDate, Date endDate, List<String> employeeCodes, List<EntryType> entryTypes);
}