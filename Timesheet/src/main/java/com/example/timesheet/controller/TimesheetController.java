package com.example.timesheet.controller;

import com.example.timesheet.enums.EntryType;
import com.example.timesheet.models.TimesheetEntry;
import com.example.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;

    @PostMapping("/create")
    public ResponseEntity<?> createTimesheet(@RequestBody TimesheetEntry entry) {
        return ResponseEntity.ok(timesheetService.createEntry(entry));
    }

    @GetMapping("/entry-types")
    public ResponseEntity<List<EntryType>> getEntryTypes() {
        return ResponseEntity.ok(timesheetService.getEntryTypes());
    }
}

