package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProjectTimeSheetEntryResponse {

    protected Long id;
    private String projectCode;
    private Long totalHoursSpent;
}
