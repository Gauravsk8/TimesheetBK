package com.example.timesheet.service.Serviceimpl;

import com.example.timesheet.Repository.DailyTimeSheetRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.UserIdentityDto;
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
        List<DailyTimeSheet> entries;

        if (projectCode != null) {
            entries = dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonthAndProjectCode(year, month, projectCode);
        } else {
            entries = dailyTimeSheetRepository.findByTimesheetYearAndTimesheetMonth(year, month);
        }

        return entries.stream()
                .filter(entry -> entry.getProjectCode() != null)
                .collect(Collectors.groupingBy(
                        DailyTimeSheet::getProjectCode,
                        Collectors.groupingBy(DailyTimeSheet::getEmployeeCode)
                ));

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
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserIdentityDto user = response.getBody();
                return user.getFirstName() + " " + user.getLastName();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch name for code: " + userCode);
        }
        return "User-" + userCode;
    }
}
