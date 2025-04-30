package com.example.timesheet.Repository;

import com.example.timesheet.models.WeeklyTimeSheet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface WeeklyTimeSheetRepository extends PagingAndSortingRepository<WeeklyTimeSheet, String>,  org.springframework.data.jpa.repository.JpaRepository<WeeklyTimeSheet, String>, QuerydslPredicateExecutor<WeeklyTimeSheet> {
    WeeklyTimeSheet findByWeekStartDate(Timestamp weekStartDate);
//    @Query("SELECT w FROM WeeklyTimeSheet w WHERE DATE(w.weekStartDate) = :dateOnly")
//    WeeklyTimeSheet findByWeekStartDateOnly(@Param("dateOnly") java.sql.Date dateOnly);



    // Convert LocalDateTime to Timestamp
   // @Query(value = "SELECT * FROM weekly_time_sheet w WHERE DATE(w.week_start_date) = :dateOnly", nativeQuery = true)
    @Query(value = "SELECT * FROM weekly_time_sheet w WHERE w.week_start_date::date = :dateOnly", nativeQuery = true)

    WeeklyTimeSheet findByWeekStartDateOnly(Timestamp dateOnly);

    WeeklyTimeSheet findByWeekStartDateAndEmployeeCode(Timestamp weekStartDate, String employeeCode);

    List<WeeklyTimeSheet> findByEmployeeCodeAndWeekStartDateBetween(String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate);



    //@Query("SELECT w FROM WeeklyTimeSheet w WHERE w.employeeCode = :employeeCode AND FUNCTION('DATE', w.weekStartDate) = :weekStartDate")
    WeeklyTimeSheet findByEmployeeCodeIgnoreCaseAndWeekStartDate(String employeeCode, Timestamp weekStartDate);

    WeeklyTimeSheet findByEmployeeCode(String employeeCode);
}
