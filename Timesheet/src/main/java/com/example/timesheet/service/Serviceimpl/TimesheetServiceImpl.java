package com.example.timesheet.service.Serviceimpl;

import com.example.common.exceptions.TimeSheetException;
import com.example.common.constants.MessageConstants;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.*;
import com.example.timesheet.dto.response.DailyTimeSheetResponseDto;
import com.example.timesheet.dto.response.EmployeeWeeklyTimesheetDto;
import com.example.timesheet.dto.response.ManagerDashboardResponseDto;
import com.example.timesheet.dto.response.TimesheetSummaryResponseDto;
import com.example.timesheet.enums.EntryType;
import com.example.timesheet.enums.TimeSheetStatus;
import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.*;
import com.example.timesheet.keys.TimesheetSummaryId;
import com.example.timesheet.Repository.*;

import com.example.timesheet.service.TimesheetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final IdentityServiceClient identityServiceClient;
    private static final DateTimeFormatter WEEK_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");


    //  Save or update a daily time entry
    @Transactional
    public String saveDailyEntry(DailyTimesheetDto dtos) {

        for (DailyTimesheetRequestDto dto : dtos.getDailyEntry()) {
            Project project = null;
            if (dto.getProjectCode() != null) {
                ProjectEmployeeId peid = new ProjectEmployeeId(dto.getProjectCode(), dto.getEmployeeCode());
                if (!projectEmployeeRepository.existsById(peid)) {
                    throw new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.ASSIGNMENT_NOT_FOUND, dto.getProjectCode(), dto.getEmployeeCode())
                    );
                }
                project = projectRepository.findById(dto.getProjectCode())
                        .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                                String.format(errorMessage.PROJECT_NOT_FOUND, dto.getProjectCode())));
            }

            DailyTimeSheet daily = new DailyTimeSheet();
            daily.setEmployeeCode(dto.getEmployeeCode());
            daily.setTimesheetYear(dto.getTimesheetYear());
            daily.setTimesheetMonth(dto.getTimesheetMonth());
            daily.setWorkDate(dto.getWorkDate());
            daily.setEntryType(dto.getEntryType());
            daily.setHoursSpent(dto.getHoursSpent());
            daily.setProjectCode(dto.getProjectCode());
            daily.setProject(project);
            daily.setDescription(dto.getDescription());
            daily.setModifiedByManager(false);

            dailyTimeSheetRepository.save(daily);
        }

        TimesheetSummaryDto summaryDto = new TimesheetSummaryDto();
        summaryDto.setEmployeeCode(dtos.getEmployeeCode());
        summaryDto.setTimesheetMonth(dtos.getTimesheetMonth());
        summaryDto.setTimesheetYear(dtos.getTimesheetYear());
        summaryDto.setWeekStart(dtos.getWeekStart()); // Assumes all entries are from the same week
        saveTimesheetSummary(summaryDto);

        return MessageConstants.DAILY_TIMESHEET_SAVED;
    }

    @Transactional
    public String UpdateDailyEntry(List<DailyTimesheetRequestDto> dtos) {

        for (DailyTimesheetRequestDto dto : dtos) {
            Project project = null;
            if (dto.getProjectCode() != null) {
                ProjectEmployeeId peid = new ProjectEmployeeId(dto.getProjectCode(), dto.getEmployeeCode());
                if (!projectEmployeeRepository.existsById(peid)) {
                    throw new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.ASSIGNMENT_NOT_FOUND, dto.getProjectCode(), dto.getEmployeeCode())
                    );
                }
                project = projectRepository.findById(dto.getProjectCode())
                        .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                                String.format(errorMessage.PROJECT_NOT_FOUND, dto.getProjectCode())));
            }

            DailyTimeSheet daily = dailyTimeSheetRepository
                    .findByEmployeeCodeAndTimesheetYearAndTimesheetMonthAndWorkDateAndEntryTypeAndProjectCode(
                            dto.getEmployeeCode(),
                            dto.getTimesheetYear(),
                            dto.getTimesheetMonth(),
                            dto.getWorkDate(),
                            dto.getEntryType(),
                            dto.getProjectCode()
                    )
                    .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,
                            errorMessage.DAILY_TIME_SHEET_NOT_FOUND_FOR_EMPLOYEE_WITHIN_DATES));

            daily.setHoursSpent(dto.getHoursSpent());
            daily.setDescription(dto.getDescription());
            daily.setProjectCode(dto.getProjectCode());
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

            List<DailyTimeSheet> existingSheets = dailyTimeSheetRepository
                    .findByEmployeeCodeAndWorkDateBetween(dto.getEmployeeCode(), weekStart, weekEnd);

            for (DailyTimesheetRequestDto requestDto : dto.getDailyTimeSheetRequests()) {
                EntryType entryType = requestDto.getEntryType();
                boolean matched = false;

                for (DailyTimeSheet sheet : existingSheets) {
                    boolean dateMatch = sheet.getWorkDate().toLocalDate().isEqual(requestDto.getWorkDate().toLocalDate());
                    boolean typeMatch = sheet.getEntryType() != null && sheet.getEntryType().name().equalsIgnoreCase(entryType.name());

                    boolean projectMatch = (entryType == EntryType.PROJECT)
                            ? Objects.equals(StringUtils.trimToNull(sheet.getProjectCode()), StringUtils.trimToNull(requestDto.getProjectCode()))
                            : true;

                    if (dateMatch && typeMatch && projectMatch) {
                        matched = true;

                        if (!Objects.equals(sheet.getHoursSpent(), requestDto.getHoursSpent())) {
                            sheet.setHoursSpent(requestDto.getHoursSpent());
                            sheet.setModifiedByManager(true);
                            dailyTimeSheetRepository.save(sheet);

                            System.out.printf("Updated sheet on %s [%s]: hours changed to %.2f%n",
                                    sheet.getWorkDate(), entryType, requestDto.getHoursSpent());
                        } else {
                            System.out.printf("No change on %s [%s], hours unchanged (%.2f)%n",
                                    sheet.getWorkDate(), entryType, sheet.getHoursSpent());
                        }
                        break;
                    }
                }

                if (!matched) {
                    System.out.printf("No match found for %s [%s] (project: %s)%n",
                            requestDto.getWorkDate(), entryType, requestDto.getProjectCode());
                }
            }
        }

        summary.setStatus(dto.isApprove() ? TimeSheetStatus.APPROVED : TimeSheetStatus.CORRECTION_REQUIRED);
        summary.setManagerComment(dto.getComment());
        summary.setApprovedBy(dto.getManagerCode());
        timesheetSummaryRepository.save(summary);

        LocalDate week = summary.getId().getWeekStart().toLocalDate();
        String formattedWeekStart = week.format(WEEK_DATE_FORMATTER);

        return dto.isApprove()
                ? String.format(MessageConstants.TIMESHEET_APPROVED_BY_MANAGER, dto.getEmployeeCode(), formattedWeekStart, dto.getManagerCode())
                : String.format(MessageConstants.TIMESHEET_REJECTED_BY_MANAGER, dto.getEmployeeCode(), formattedWeekStart, dto.getManagerCode());
    }




    // 4. Get summaries for an employee
    public List<TimesheetSummaryResponseDto> getEmployeeTimesheetSummaries(String employeeCode, Integer year, Integer month) {
        return timesheetSummaryRepository
                .findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(employeeCode, year, month)
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
                })
                .collect(Collectors.toList());
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
                .findByEmployeeCodeAndWorkDateBetween(employeeCode, weekStart, weekEnd)
                .stream()
                .map(d -> {
                    DailyTimeSheetResponseDto dto = new DailyTimeSheetResponseDto();
                    dto.setEmployeeCode(d.getEmployeeCode());
                    dto.setTimesheetYear(d.getTimesheetYear());
                    dto.setTimesheetMonth(d.getTimesheetMonth());
                    dto.setWorkDate(d.getWorkDate());
                    dto.setEntryType(d.getEntryType());
                    dto.setProjectCode(d.getProjectCode());
                    dto.setDescription(d.getDescription());
                    dto.setHoursSpent(d.getHoursSpent());
                    dto.setModifiedByManager(d.getModifiedByManager());
                    dto.setStatus(summary.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 6. Generate monthly report for a project
    public List<EmployeeWeeklyTimesheetDto> getWeeklyTimesheetsForProject(String projectCode, Integer year, Integer month) {
        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByIdProjectCode(projectCode);
        List<EmployeeWeeklyTimesheetDto> result = new ArrayList<>();

        for (ProjectEmployee pe : projectEmployees) {
            String employeeCode = pe.getId().getEmployeeCode();

            List<TimesheetSummary> timesheets = timesheetSummaryRepository
                    .findByIdEmployeeCodeAndIdTimesheetYearAndIdTimesheetMonth(employeeCode, year, month);

            for (TimesheetSummary ts : timesheets) {
                EmployeeWeeklyTimesheetDto dto = new EmployeeWeeklyTimesheetDto();
                dto.setEmployeeCode(employeeCode);
                dto.setWeekStart(ts.getId().getWeekStart());
                dto.setStatus(ts.getStatus());

                // Calculate week end date from week start
                Date weekStart = ts.getId().getWeekStart();
                Calendar cal = Calendar.getInstance();
                cal.setTime(weekStart);
                cal.add(Calendar.DATE, 6);
                Date weekEnd = new Date(cal.getTimeInMillis());

                // Get total hours spent by employee on this project for this week
                Double totalHours = dailyTimeSheetRepository.sumHoursSpentByEmployeeProjectAndWeek(employeeCode, projectCode, weekStart, weekEnd);

                dto.setHoursSpent(totalHours == null ? 0.0 : totalHours);

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
                .findByEmployeeCodeAndWorkDateBetween(dto.getEmployeeCode(), weekStart, weekEnd);

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

    @Override
    @Transactional
    public String approveAllUnderManagerForWeek(ManagerApprovalRequestDto approvalRequest) throws TimeSheetException {
        // 1. Get all employees under this manager
        ResponseEntity<List<UserIdentityDto>> response = identityServiceClient
                .getEmployeesUnderManager(approvalRequest.getManagerCode());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new TimeSheetException(errorCode.FAILED_TO_FETCH_DETAILS, errorMessage.USERID_EXTRACTION_FAILED);
        } else {
            response.getBody();
        }

        List<String> employeeCodes = response.getBody().stream()
                .map(UserIdentityDto::getEmployeeCode)
                .toList();

        // 2. Find all timesheet summaries for these employees for the specified week
        List<TimesheetSummary> summaries = timesheetSummaryRepository
                .findByIdEmployeeCodeInAndIdWeekStartAndIdTimesheetYearAndIdTimesheetMonth(
                        employeeCodes,
                        approvalRequest.getWeekStart(),
                        approvalRequest.getTimesheetYear(),
                        approvalRequest.getTimesheetMonth()
                );

        // 3. Approve each timesheet
        summaries.forEach(summary -> {
            summary.setStatus(TimeSheetStatus.APPROVED);
            summary.setApprovedBy(approvalRequest.getManagerCode());
            summary.setManagerComment(approvalRequest.getComment());
        });

        timesheetSummaryRepository.saveAll(summaries);

        return String.format(MessageConstants.APPROVED_ALL_TIMESHEETS_FOR_WEEK,
                summaries.size(),
                approvalRequest.getWeekStart(),
                approvalRequest.getManagerCode());
    }

    @Override
    @Transactional
    public List<ManagerDashboardResponseDto> getEmployeesTimesheetUnderManager(String managerCode, int year, int month) {
        ResponseEntity<List<UserIdentityDto>> response = identityServiceClient.getEmployeesUnderManager(managerCode);
        List<String> employeeCodes = Optional.ofNullable(response.getBody())
                .orElse(Collections.emptyList())
                .stream()
                .map(UserIdentityDto::getEmployeeCode)
                .collect(Collectors.toList());


        if (employeeCodes.isEmpty()) return List.of();

        List<TimesheetSummary> summaries = timesheetSummaryRepository
                .findByIdEmployeeCodeInAndIdTimesheetYearAndIdTimesheetMonth(employeeCodes, year, month);

        Map<String, List<WeeklyTimeSheetEntryDto>> grouped = new HashMap<>();

        for (TimesheetSummary summary : summaries) {
            String empCode = summary.getId().getEmployeeCode();
            grouped.computeIfAbsent(empCode, k -> new ArrayList<>())
                    .add(toWeeklyEntryDto(summary));
        }

        return grouped.entrySet()
                .stream()
                .map(entry -> new ManagerDashboardResponseDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ManagerDashboardResponseDto::getEmployeeCode))
                .toList();
    }

    private WeeklyTimeSheetEntryDto toWeeklyEntryDto(TimesheetSummary s) {
        LocalDate start = toLocalDate(s.getId().getWeekStart());
        String startStr = start.toString();
        String endStr = start.plusDays(6).toString();

        return new WeeklyTimeSheetEntryDto(
                startStr,
                endStr,
                s.getTotalHours(),
                s.getStatus().name()
        );
    }

    @Override
    public TimeSheetStatus getWeeklyStatus(String employeeCode, Date weekStart) {

        TimesheetSummary summary = timesheetSummaryRepository
                .findByIdEmployeeCodeAndIdWeekStart(employeeCode, weekStart)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.TIMESHEET_SUMMARY_NOT_FOUND, employeeCode, weekStart)
                ));
        return summary.getStatus();
    }
   /* private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }*/


    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
