package com.example.timesheet.service.Serviceimpl;

import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.enums.EntryType;
import com.example.timesheet.models.DailyTimeSheet;
import com.example.timesheet.models.Project;
import com.example.timesheet.service.TimesheetReportService;
import com.example.timesheet.utils.ExcelReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public Map<String, Map<String, List<DailyTimeSheet>>> getMonthlyTimesheetData(int year, int month, String projectCode) {
        // 1. Fetch project-specific entries based on projectCode filter
        List<DailyTimeSheet> projectEntries;
        if (projectCode != null) {
            projectEntries = dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonthAndProjectCode(year, month, projectCode);
        } else {
            projectEntries = dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonth(year, month);
        }

        // 2. Group project entries by projectCode then employeeCode
        Map<String, Map<String, List<DailyTimeSheet>>> projectEmpEntries = projectEntries.stream()
                .filter(entry -> entry.getProjectCode() != null)
                .collect(Collectors.groupingBy(
                        DailyTimeSheet::getProjectCode,
                        Collectors.groupingBy(DailyTimeSheet::getEmployeeCode)
                ));


        // 3. Collect all unique employeeCodes from project entries
        Set<String> employeeCodes = projectEntries.stream()
                .map(DailyTimeSheet::getEmployeeCode)
                .collect(Collectors.toSet());

        if (employeeCodes.isEmpty()) {
            // no entries found
            return Collections.emptyMap();
        }

        // 4. Fetch LEAVE and HOLIDAY entries for all employees (all projects)
        List<EntryType> leaveTypes = List.of(EntryType.LEAVE, EntryType.HOLIDAY);
        List<DailyTimeSheet> leaveEntries = dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonthAndEmployeeCodeInAndEntryTypeIn(year, month, new ArrayList<>(employeeCodes), leaveTypes);

        // 5. Group leave entries by employeeCode for quick lookup
        Map<String, List<DailyTimeSheet>> leaveEntriesByEmployee = leaveEntries.stream()
                .collect(Collectors.groupingBy(DailyTimeSheet::getEmployeeCode));

        // 6. For each project and employee, add leave entries for that employee into their list
        for (Map.Entry<String, Map<String, List<DailyTimeSheet>>> projectEntry : projectEmpEntries.entrySet()) {
            Map<String, List<DailyTimeSheet>> empMap = projectEntry.getValue();

            for (String empCode : empMap.keySet()) {
                List<DailyTimeSheet> leaves = leaveEntriesByEmployee.get(empCode);
                if (leaves != null) {
                    empMap.get(empCode).addAll(leaves);
                    // Optionally sort combined list by date
                    empMap.get(empCode).sort(Comparator.comparing(DailyTimeSheet::getWorkDate));
                }
            }
        }

        return projectEmpEntries;
    }

    @Override
    public ResponseEntity<String> generateReport(int year, int month, String projectCode) {
        try {
            String monthLabel = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + "-" + year;
            String baseDir = "timesheet-reports";

            // Get combined data with leave entries merged in project-wise employee maps
            Map<String, Map<String, List<DailyTimeSheet>>> data = getMonthlyTimesheetData(year, month, projectCode);

            for (String projCode : data.keySet()) {
                Project project = projectRepository.findById(projCode).orElse(null);
                if (project == null) continue;

                String projectName = project.getTitle();
                String managerCode = project.getProjectManagerCode();
                String managerName = getUserName(managerCode);

                Map<String, List<DailyTimeSheet>> empEntries = data.get(projCode);
                for (Map.Entry<String, List<DailyTimeSheet>> entry : empEntries.entrySet()) {
                    String empCode = entry.getKey();
                    String empName = getUserName(empCode);
                    List<DailyTimeSheet> timesheetEntries = entry.getValue();

                    ExcelReportGenerator.generateExcel(
                            baseDir,
                            monthLabel,
                            projectName,
                            managerName,
                            empName,
                            timesheetEntries
                    );
                }
            }

            return ResponseEntity.ok("Reports generated at: " + baseDir + " for month: " + monthLabel);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to generate report: " + e.getMessage());
        }
    }


    private String getUserName(String userCode) {
        try {
            ResponseEntity<UserIdentityDto> response = identityServiceClient.getUserByemployeeCode(userCode);
            if (response.getStatusCode().is2xxSuccessful()) {
                UserIdentityDto user = response.getBody();
                if (user != null) {
                    String name = user.getFirstName() + " " + user.getLastName();
                    System.out.println("Resolved name for " + userCode + ": " + name);
                    return name;
                } else {
                    System.err.println("Response body was null for userCode: " + userCode);
                }
            } else {
                System.err.println("Non-200 response for " + userCode + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch name for userCode: " + userCode + ", error: " + e.getMessage());
        }
        return "User-" + userCode;
    }

}
