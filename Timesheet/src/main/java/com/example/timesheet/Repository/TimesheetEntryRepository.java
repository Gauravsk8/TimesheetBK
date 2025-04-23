package com.example.timesheet.Repository;

import com.example.timesheet.models.TimesheetEntry;
import com.example.timesheet.models.UserProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {
}
