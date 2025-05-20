package com.example.timesheet.service;


import com.example.timesheet.models.DailyTimeSheet;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TimesheetReportService {

    Map<String, Map<String, List<DailyTimeSheet>>> getMonthlyTimesheetData(int year, int month, String projectCode);

    ResponseEntity<String> generateReport(int year, int month, String projectCode);}
