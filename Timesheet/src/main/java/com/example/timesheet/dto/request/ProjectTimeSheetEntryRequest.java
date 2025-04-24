package com.example.timesheet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProjectTimeSheetEntryRequest {
    private Long projectId;
    private Long totalHoursSpent;
}
