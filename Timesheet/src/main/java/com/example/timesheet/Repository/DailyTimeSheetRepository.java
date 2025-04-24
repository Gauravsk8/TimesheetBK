package com.example.timesheet.Repository;

import com.example.timesheet.models.DailyTimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface DailyTimeSheetRepository extends
        JpaRepository<DailyTimeSheet, Long>,
        PagingAndSortingRepository<DailyTimeSheet, Long>,
        QuerydslPredicateExecutor<DailyTimeSheet> {

    DailyTimeSheet findByDate(Date date);

    @Query("SELECT d FROM DailyTimeSheet d WHERE d.employeeCode = :employeeCode AND DATE(d.date) BETWEEN :startDate AND :endDate")
    List<DailyTimeSheet> findByEmployeeCodeAndDateBetween(@Param("employeeCode") String employeeCode,
                                                          @Param("startDate") Timestamp startDate,
                                                          @Param("endDate") Timestamp endDate);

    DailyTimeSheet findByDateAndEmployeeCode(Timestamp date, String employeeCode);

    // List<DailyTimeSheet> findByEmployeeCodeAndDateBetween(String employeeCode, Timestamp weekStartDate, Timestamp weekEndDate);
}
