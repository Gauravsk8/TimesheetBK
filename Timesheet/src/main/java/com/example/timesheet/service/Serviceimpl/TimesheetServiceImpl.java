package com.example.timesheet.service.Serviceimpl;

import com.example.common.exceptions.TimeSheetException;
import com.example.common.constants.MessageConstants;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.timesheet.dto.request.DailyTimeSheetRequestDto;
import com.example.timesheet.dto.request.ManagerApprovalRequestDto;
import com.example.timesheet.dto.request.TimesheetSummaryDto;
import com.example.timesheet.dto.response.DailyTimeSheetResponseDto;
import com.example.timesheet.dto.response.EmployeeWeeklyTimesheetDto;
import com.example.timesheet.dto.response.TimesheetSummaryResponseDto;
import com.example.timesheet.enums.TimeSheetStatus;
import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.*;
import com.example.timesheet.keys.DailyTimeSheetId;
import com.example.timesheet.keys.TimesheetSummaryId;
import com.example.timesheet.Repository.*;

import com.example.timesheet.service.TimesheetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimesheetServiceImpl implements TimesheetService{

    private final DailyTimeSheetRepository dailyTimeSheetRepository;
    private final TimesheetSummaryRepository timesheetSummaryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private static final DateTimeFormatter WEEK_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");


    //  Save or update a daily time entry
    @Transactional
    public String saveDailyEntry(List<DailyTimeSheetRequestDto> dtos) {

        DailyTimeSheetRequestDto first = dtos.get(0);

        for (DailyTimeSheetRequestDto dto : dtos) {
            Project project = null;
            if (dto.getProjectCode() != null) {
                ProjectEmployeeId peid = new ProjectEmployeeId(dto.getProjectCode().toLowerCase(), dto.getEmployeeCode().toLowerCase());
                if (!projectEmployeeRepository.existsById(peid)) {
                    throw new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,  // Assuming this is the error code
                            String.format(errorMessage.ASSIGNMENT_NOT_FOUND, dto.getProjectCode(), dto.getEmployeeCode())  // Assuming this error message exists
                    );
                }
                project = projectRepository.findById(dto.getProjectCode().toLowerCase())
                        .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                                String.format(errorMessage.PROJECT_NOT_FOUND, dto.getProjectCode())));
            }

            DailyTimeSheetId id = new DailyTimeSheetId(
                    dto.getEmployeeCode(),
                    dto.getTimesheetYear(),
                    dto.getTimesheetMonth(),
                    dto.getWorkDate(),
                    dto.getProjectCode(),
                    dto.getEntryType()
            );

            DailyTimeSheet daily = new DailyTimeSheet();
            daily.setId(id);
            daily.setHoursSpent(dto.getHoursSpent());
            daily.setProject(project);
            daily.setDescription(dto.getDescription());
            daily.setModifiedByManager(false);

            dailyTimeSheetRepository.save(daily);
        }
        TimesheetSummaryDto summaryDto = new TimesheetSummaryDto();
        summaryDto.setEmployeeCode(first.getEmployeeCode());
        summaryDto.setTimesheetMonth(first.getTimesheetMonth());
        summaryDto.setTimesheetYear(first.getTimesheetYear());
        summaryDto.setWeekStart(first.getWorkDate()); // Assumes all entries are from the same week
        saveTimesheetSummary(summaryDto);

        return MessageConstants.DAILY_TIMESHEET_SAVED;
    }

    @Transactional
    public String UpdateDailyEntry(List<DailyTimeSheetRequestDto> dtos) {

        for (DailyTimeSheetRequestDto dto : dtos) {
            Project project = null;
            if (dto.getProjectCode() != null) {
                ProjectEmployeeId peid = new ProjectEmployeeId(dto.getProjectCode().toLowerCase(), dto.getEmployeeCode().toLowerCase());
                if (!projectEmployeeRepository.existsById(peid)) {
                    throw new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,  // Assuming this is the error code
                            String.format(errorMessage.ASSIGNMENT_NOT_FOUND, dto.getProjectCode(), dto.getEmployeeCode())  // Assuming this error message exists
                    );
                }
                project = projectRepository.findById(dto.getProjectCode().toLowerCase())
                        .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                                String.format(errorMessage.PROJECT_NOT_FOUND, dto.getProjectCode())));
            }

            DailyTimeSheetId id = new DailyTimeSheetId(
                    dto.getEmployeeCode(),
                    dto.getTimesheetYear(),
                    dto.getTimesheetMonth(),
                    dto.getWorkDate(),
                    dto.getProjectCode(),
                    dto.getEntryType()
            );

            DailyTimeSheet daily = dailyTimeSheetRepository.findById(id)
                    .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                            errorMessage.DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES));
            daily.setId(id);
            daily.setHoursSpent(dto.getHoursSpent());
            daily.setDescription(dto.getDescription());
            daily.setProject(project);
            daily.setModifiedByManager(false);

            dailyTimeSheetRepository.save(daily);
        }

        return MessageConstants.DAILY_TIMESHEET_SAVED;
    }

    //  Submit weekly timesheet
    @Transactional
    public String submitTimesheetSummary(TimesheetSummaryDto dto) {

        TimesheetSummaryId id = new TimesheetSummaryId(
                dto.getEmployeeCode(),
                dto.getTimesheetYear(),
                dto.getTimesheetMonth(),
                dto.getWeekStart()
        );

        TimesheetSummary summary = timesheetSummaryRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.TIMESHEET_SUMMARY_NOT_FOUND,
                                dto.getEmployeeCode(), dto.getWeekStart()))
                );

        if (summary.getStatus() != TimeSheetStatus.DRAFT) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,
                    errorMessage.STATUS_NOT_FOUND);
        }
        summary.setStatus(TimeSheetStatus.SUBMITTED);
        summary.setSubmittedDate(new Timestamp(System.currentTimeMillis()));

        TimesheetSummary submitted=timesheetSummaryRepository.save(summary);

        LocalDate week = submitted.getId().getWeekStart().toLocalDate();
        String formattedWeekStart = week.format(WEEK_DATE_FORMATTER);

        return String.format(
                MessageConstants.SUBMITTED_TIMESHEET,
                submitted.getId().getEmployeeCode(),
                formattedWeekStart,
                submitted.getId().getTimesheetMonth(),
                submitted.getId().getTimesheetYear()
        );


    }

    // Approve or reject timesheet weekly by manager
    @Transactional
    public String approveOrRejectWeekly(ManagerApprovalRequestDto dto) {
        TimesheetSummaryId id = new TimesheetSummaryId(
                dto.getEmployeeCode(),
                dto.getTimesheetYear(),
                dto.getTimesheetMonth(),
                dto.getWeekStart()
        );

        TimesheetSummary summary = timesheetSummaryRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.TIMESHEET_SUMMARY_NOT_FOUND,
                                dto.getEmployeeCode(), dto.getWeekStart()))
                );

        if (dto.getDailyTimeSheetRequests() != null && !dto.getDailyTimeSheetRequests().isEmpty()) {
            Date weekStart = dto.getWeekStart();
            Date weekEnd = Date.valueOf(weekStart.toLocalDate().plusDays(6));
            List<DailyTimeSheet> dailyTimeSheets = dailyTimeSheetRepository
                    .findByIdEmployeeCodeAndIdWorkDateBetween(dto.getEmployeeCode(), weekStart, weekEnd);

            Map<DailyTimeSheetId, DailyTimeSheet> existingSheetsMap = dailyTimeSheets.stream()
                    .collect(Collectors.toMap(DailyTimeSheet::getId, Function.identity()));

            for (DailyTimeSheetRequestDto requestDto : dto.getDailyTimeSheetRequests()) {
                DailyTimeSheetId requestId = new DailyTimeSheetId(
                        requestDto.getEmployeeCode(),
                        requestDto.getTimesheetYear(),
                        requestDto.getTimesheetMonth(),
                        requestDto.getWorkDate(),
                        requestDto.getProjectCode(),
                        requestDto.getEntryType()
                );

                DailyTimeSheet existingSheet = existingSheetsMap.get(requestId);
                if (existingSheet != null) {
                    if (!Objects.equals(existingSheet.getHoursSpent(), requestDto.getHoursSpent())) {
                        existingSheet.setHoursSpent(requestDto.getHoursSpent());
                        existingSheet.setModifiedByManager(true);
                        dailyTimeSheetRepository.save(existingSheet);
                    }
                }
            }
        }

        summary.setStatus(dto.isApprove() ? TimeSheetStatus.APPROVED : TimeSheetStatus.CORRECTION_REQUIRED);
        summary.setManagerComment(dto.getComment());
        summary.setApprovedBy(dto.getManagerCode());

        timesheetSummaryRepository.save(summary);
        TimesheetSummary submitted=timesheetSummaryRepository.save(summary);

        LocalDate week = submitted.getId().getWeekStart().toLocalDate();
        String formattedWeekStart = week.format(WEEK_DATE_FORMATTER);

        return dto.isApprove()
                ? String.format(MessageConstants.TIMESHEET_APPROVED_BY_MANAGER, dto.getEmployeeCode(), formattedWeekStart, dto.getManagerCode())
                : String.format(MessageConstants.TIMESHEET_REJECTED_BY_MANAGER, dto.getEmployeeCode(), formattedWeekStart, dto.getManagerCode());
    }

    // 4. Get summaries for an employee
    public List<TimesheetSummaryResponseDto> getEmployeeTimesheetSummaries(String employeeCode) {
        return timesheetSummaryRepository.findByIdEmployeeCode(employeeCode)
                .stream()
                .map(summary -> {
                    TimesheetSummaryResponseDto dto = new TimesheetSummaryResponseDto();
                    dto.setEmployeeCode(summary.getId().getEmployeeCode());
                    dto.setTimesheetYear(summary.getId().getTimesheetYear());
                    dto.setTimesheetMonth(summary.getId().getTimesheetMonth());
                    dto.setWeekStart(summary.getId().getWeekStart());
                    dto.setTotalHours(summary.getTotalHours());
                    dto.setStatus(summary.getStatus());
                    dto.setSubmittedDate(summary.getSubmittedDate());
                    dto.setManagerComment(summary.getManagerComment());
                    dto.setApprovedBy(summary.getApprovedBy());
                    return dto;
                }).collect(Collectors.toList());
    }

    // 5. Get daily entries for employee for a given week
    public List<DailyTimeSheetResponseDto> getDailyEntries(String employeeCode, Date weekStart) {
        Date weekEnd = Date.valueOf(weekStart.toLocalDate().plusDays(6));

        TimesheetSummary summary = timesheetSummaryRepository
                .findByIdEmployeeCodeAndIdWeekStart(employeeCode, weekStart)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.TIMESHEET_SUMMARY_NOT_FOUND, employeeCode, weekStart)
                ));


        return dailyTimeSheetRepository
                .findByIdEmployeeCodeAndIdWorkDateBetween(employeeCode, weekStart, weekEnd)
                .stream()
                .map(d -> {
                    DailyTimeSheetResponseDto dto = new DailyTimeSheetResponseDto();
                    dto.setEmployeeCode(d.getId().getEmployeeCode());
                    dto.setTimesheetYear(d.getId().getTimesheetYear());
                    dto.setTimesheetMonth(d.getId().getTimesheetMonth());
                    dto.setWorkDate(d.getId().getWorkDate());
                    dto.setEntryType(d.getId().getEntryType());
                    dto.setProjectCode(d.getId().getProjectCode());
                    dto.setDescription(d.getDescription());
                    dto.setHoursSpent(d.getHoursSpent());
                    dto.setModifiedByManager(d.getModifiedByManager());
                    dto.setStatus(summary.getStatus());
                    return dto;
                }).collect(Collectors.toList());
    }

    // 6. Generate monthly report for a project
    public List<EmployeeWeeklyTimesheetDto> getWeeklyTimesheetsForProject(String projectCode, Integer year, Integer month) {
        // 1. Get employees assigned to the project
        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByIdProjectCode(projectCode);

        // 2. For each employee get their weekly timesheets from TimesheetSummary
        List<EmployeeWeeklyTimesheetDto> result = new ArrayList<>();

        for (ProjectEmployee pe : projectEmployees) {
            String employeeCode = pe.getId().getEmployeeCode();

            // fetch all weekly timesheets for this employee in given year/month
            List<TimesheetSummary> timesheets = timesheetSummaryRepository
                    .findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(employeeCode, year, month);

            for (TimesheetSummary ts : timesheets) {
                EmployeeWeeklyTimesheetDto dto = new EmployeeWeeklyTimesheetDto();
                dto.setEmployeeCode(employeeCode);
                dto.setWeekStart(ts.getId().getWeekStart());
                dto.setStatus(ts.getStatus());
                result.add(dto);
            }
        }
        return result;
    }


    @Transactional
    public void saveTimesheetSummary(TimesheetSummaryDto dto) {
        Date weekStart = dto.getWeekStart();
        Date weekEnd = Date.valueOf(weekStart.toLocalDate().plusDays(6));

        List<DailyTimeSheet> entries = dailyTimeSheetRepository
                .findByIdEmployeeCodeAndIdWorkDateBetween(dto.getEmployeeCode(), weekStart, weekEnd);

        double total = entries.stream().mapToDouble(DailyTimeSheet::getHoursSpent).sum();

        TimesheetSummaryId summaryId = new TimesheetSummaryId(
                dto.getEmployeeCode(),
                dto.getTimesheetYear(),
                dto.getTimesheetMonth(),
                dto.getWeekStart()
        );

        TimesheetSummary summary = new TimesheetSummary();
        summary.setId(summaryId);
        summary.setTotalHours(total);
        summary.setStatus(TimeSheetStatus.DRAFT);
        summary.setSubmittedDate(new Timestamp(System.currentTimeMillis()));
        timesheetSummaryRepository.save(summary);

    }
}
