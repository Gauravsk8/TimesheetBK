package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.DailyTimesheetDto;
import com.example.timesheet.dto.request.DailyTimesheetRequestDto;
import com.example.timesheet.dto.request.ManagerApprovalRequestDto;
import com.example.timesheet.dto.request.TimesheetSummaryDto;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.enums.TimeSheetStatus;

import java.sql.Date;
import java.util.List;

public interface TimesheetService {
    String saveDailyEntry(DailyTimesheetDto dtos) throws TimeSheetException;
    String UpdateDailyEntry(List<DailyTimesheetRequestDto> dtos) throws TimeSheetException;
    String submitTimesheetSummary(TimesheetSummaryDto dto) throws TimeSheetException;
    String approveOrRejectWeekly(ManagerApprovalRequestDto dto) throws TimeSheetException;
    List<TimesheetSummaryResponseDto> getEmployeeTimesheetSummaries(String employeeCode, Integer year, Integer month);

    List<DailyTimeSheetResponseDto> getDailyEntries(String employeeCode, Date weekStart) throws TimeSheetException;
    List<EmployeeWeeklyTimesheetDto> getWeeklyTimesheetsForProject(String projectCode, Integer year, Integer month);
    void saveTimesheetSummary(TimesheetSummaryDto dto);
    String approveAllUnderManagerForWeek(ManagerApprovalRequestDto approvalRequest) throws TimeSheetException;
    List<TimesheetMatrixRowResponseDto> getEmployeeTimesheetMatrix(String employeeCode, Integer year, Integer month);
    List<ManagerDashboardResponseDto> getEmployeesTimesheetUnderManager(String managerCode, int year, int month) throws TimeSheetException;

    TimeSheetStatus getWeeklyStatus(String employeeCode, Date weekStart);
}