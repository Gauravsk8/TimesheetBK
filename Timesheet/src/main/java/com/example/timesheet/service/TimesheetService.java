package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.DailyTimeSheetRequestDto;
import com.example.timesheet.dto.request.ManagerApprovalRequestDto;
import com.example.timesheet.dto.request.TimesheetSummaryDto;
import com.example.timesheet.dto.response.DailyTimeSheetResponseDto;
import com.example.timesheet.dto.response.EmployeeWeeklyTimesheetDto;
import com.example.timesheet.dto.response.TimesheetSummaryResponseDto;
import com.example.timesheet.enums.TimeSheetStatus;

import java.sql.Date;
import java.util.List;

public interface TimesheetService {
    String saveDailyEntry(List<DailyTimeSheetRequestDto> dtos) throws TimeSheetException;
    String UpdateDailyEntry(List<DailyTimeSheetRequestDto> dtos) throws TimeSheetException;
    String submitTimesheetSummary(TimesheetSummaryDto dto) throws TimeSheetException;
    String approveOrRejectWeekly(ManagerApprovalRequestDto dto) throws TimeSheetException;
    List<TimesheetSummaryResponseDto> getEmployeeTimesheetSummaries(String employeeCode);
    List<DailyTimeSheetResponseDto> getDailyEntries(String employeeCode, Date weekStart) throws TimeSheetException;
    List<EmployeeWeeklyTimesheetDto> getWeeklyTimesheetsForProject(String projectCode, Integer year, Integer month);
    void saveTimesheetSummary(TimesheetSummaryDto dto);
}