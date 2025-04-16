package com.example.timesheet.Repository;


import com.example.timesheet.models.WeeklyTimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyTimesheetRepository extends JpaRepository<WeeklyTimeSheet, Long> {
}
