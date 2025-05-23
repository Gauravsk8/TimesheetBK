package com.example.timesheet.service.Serviceimpl;

import com.example.common.constants.ErrorCode;
import com.example.common.constants.ErrorMessage;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.response.UserIdentityDto;
import com.example.timesheet.enums.EntryType;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.Project;
import com.example.timesheet.service.TimesheetReportService;
import com.example.timesheet.utils.ExcelReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimesheetReportServiceImpl implements TimesheetReportService {

    private final DailyTimeSheetRepository dailyTimeSheetRepository;
    private final ProjectRepository projectRepository;
    private final IdentityServiceClient identityServiceClient;

    @Override
    public ResponseEntity<String> generateReport(Integer year, Integer month, String projectCode, Date startDate, Date endDate) {
        try {
            String label;
            List<DailyTimeSheet> timesheetEntries;

            if (startDate != null && endDate != null) {
                // Report by date range
                label = new SimpleDateFormat("dd-MM-yyyy").format(startDate) + "_to_" +
                        new SimpleDateFormat("dd-MM-yyyy").format(endDate);
                timesheetEntries = (projectCode != null)
                        ? dailyTimeSheetRepository.findByWorkDateBetweenAndProjectCode(startDate, endDate, projectCode)
                        : dailyTimeSheetRepository.findByWorkDateBetween(startDate, endDate);
            } else if (year != null && month != null) {
                // Delegate to monthly logic
                return generateReport(year, month, projectCode);
            } else {
                return ResponseEntity.badRequest().body("Provide either (year & month) or (startDate & endDate)");
            }

            if (timesheetEntries.isEmpty()) {
                return ResponseEntity.ok("No data found for provided criteria.");
            }

            // Prepare report
            String baseDir = "timesheet-reports";

            // Grouping: project → employee → entries
            Map<String, Map<String, List<DailyTimeSheet>>> projectEmpMap = timesheetEntries.stream()
                    .filter(e -> e.getProjectCode() != null)
                    .collect(Collectors.groupingBy(
                            DailyTimeSheet::getProjectCode,
                            Collectors.groupingBy(DailyTimeSheet::getEmployeeCode)
                    ));

            // Get LEAVE/HOLIDAY entries for those employees
            Set<String> employeeCodes = timesheetEntries.stream()
                    .map(DailyTimeSheet::getEmployeeCode)
                    .collect(Collectors.toSet());

            List<EntryType> leaveTypes = List.of(EntryType.LEAVE, EntryType.HOLIDAY);
            List<DailyTimeSheet> leaveEntries = dailyTimeSheetRepository
                    .findByWorkDateBetweenAndEmployeeCodeInAndEntryTypeIn(startDate, endDate, new ArrayList<>(employeeCodes), leaveTypes);

            Map<String, List<DailyTimeSheet>> leaveByEmp = leaveEntries.stream()
                    .collect(Collectors.groupingBy(DailyTimeSheet::getEmployeeCode));

            // Merge leave entries into map
            for (Map.Entry<String, Map<String, List<DailyTimeSheet>>> projEntry : projectEmpMap.entrySet()) {
                for (Map.Entry<String, List<DailyTimeSheet>> empEntry : projEntry.getValue().entrySet()) {
                    List<DailyTimeSheet> leaves = leaveByEmp.get(empEntry.getKey());
                    if (leaves != null) {
                        empEntry.getValue().addAll(leaves);
                        empEntry.getValue().sort(Comparator.comparing(DailyTimeSheet::getWorkDate));
                    }
                }
            }

            // Generate Excel reports
            for (String projCode : projectEmpMap.keySet()) {
                Project project = projectRepository.findById(projCode).orElse(null);
                if (project == null) continue;

                String projectName = project.getTitle();
                String managerName = getUserName(project.getProjectManagerCode());

                Map<String, List<DailyTimeSheet>> empEntries = projectEmpMap.get(projCode);
                for (Map.Entry<String, List<DailyTimeSheet>> entry : empEntries.entrySet()) {
                    String empName = getUserName(entry.getKey());
                    ExcelReportGenerator.generateExcel(baseDir, label, projectName, managerName, empName, entry.getValue());
                }
            }

            return ResponseEntity.ok("Report generated for: " + label);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error generating report: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> generateReport(int year, int month, String projectCode) {
        try {
            String monthLabel = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + "-" + year;
            String baseDir = "timesheet-reports";

            Map<String, Map<String, List<DailyTimeSheet>>> data = getMonthlyTimesheetData(year, month, projectCode);

            for (String projCode : data.keySet()) {
                Project project = projectRepository.findById(projCode).orElse(null);
                if (project == null) continue;

                String projectName = project.getTitle();
                String managerName = getUserName(project.getProjectManagerCode());

                Map<String, List<DailyTimeSheet>> empEntries = data.get(projCode);
                for (Map.Entry<String, List<DailyTimeSheet>> entry : empEntries.entrySet()) {
                    String empName = getUserName(entry.getKey());
                    ExcelReportGenerator.generateExcel(baseDir, monthLabel, projectName, managerName, empName, entry.getValue());
                }
            }

            return ResponseEntity.ok("Reports generated at: " + baseDir + " for month: " + monthLabel);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to generate report: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Map<String, List<DailyTimeSheet>>> getMonthlyTimesheetData(int year, int month, String projectCode) {
        List<DailyTimeSheet> entries = (projectCode != null)
                ? dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonthAndProjectCode(year, month, projectCode)
                : dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonth(year, month);

        Map<String, Map<String, List<DailyTimeSheet>>> projectEmpMap = entries.stream()
                .filter(e -> e.getProjectCode() != null)
                .collect(Collectors.groupingBy(
                        DailyTimeSheet::getProjectCode,
                        Collectors.groupingBy(DailyTimeSheet::getEmployeeCode)
                ));

        Set<String> employeeCodes = entries.stream()
                .map(DailyTimeSheet::getEmployeeCode)
                .collect(Collectors.toSet());

        if (employeeCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        List<DailyTimeSheet> leaveEntries = dailyTimeSheetRepository
                .findByTimesheetYearAndTimesheetMonthAndEmployeeCodeInAndEntryTypeIn(
                        year, month, new ArrayList<>(employeeCodes), List.of(EntryType.LEAVE, EntryType.HOLIDAY)
                );

        Map<String, List<DailyTimeSheet>> leaveMap = leaveEntries.stream()
                .collect(Collectors.groupingBy(DailyTimeSheet::getEmployeeCode));

        for (Map<String, List<DailyTimeSheet>> empMap : projectEmpMap.values()) {
            for (Map.Entry<String, List<DailyTimeSheet>> empEntry : empMap.entrySet()) {
                List<DailyTimeSheet> leaves = leaveMap.get(empEntry.getKey());
                if (leaves != null) {
                    empEntry.getValue().addAll(leaves);
                    empEntry.getValue().sort(Comparator.comparing(DailyTimeSheet::getWorkDate));
                }
            }
        }

        return projectEmpMap;
    }

    private String getUserName(String userCode) {
        try {
            ResponseEntity<UserIdentityDto> response = identityServiceClient.getUserByemployeeCode(userCode);
            if (response.getStatusCode().is2xxSuccessful()) {
                UserIdentityDto user = response.getBody();
                return user.getFirstName() + " " + user.getLastName();
            }
        } catch (Exception e) {
            throw new TimeSheetException(ErrorCode.NOT_FOUND_ERROR, ErrorMessage.USER_NOT_FOUND + e);
        }
        return "User-" + userCode;
    }
}
