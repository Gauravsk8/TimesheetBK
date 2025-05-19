package com.example.timesheet.dto.response;

import com.example.timesheet.dto.request.WeeklyTimeSheetEntryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponseDto {
    private String employeeCode;
    private List<WeeklyTimeSheetEntryDto> weeklySummaries;
}
