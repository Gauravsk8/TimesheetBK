// DailyTimeSheetRepository.java
package com.example.timesheet.Repository;

import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.keys.DailyTimeSheetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface DailyTimeSheetRepository extends JpaRepository<DailyTimeSheet, DailyTimeSheetId> {

    List<DailyTimeSheet> findByIdEmployeeCodeAndIdWorkDateBetween(String employeeCode, Date startDate, Date endDate);

    List<DailyTimeSheet> findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(String employeeCode, Integer year, Integer month);


}
