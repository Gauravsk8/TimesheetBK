package com.example.timesheet.dto.response;

import com.example.timesheet.models.DailyTimeSheet;
import jakarta.persistence.*;
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
    private Long projectId;
    private Long totalHoursSpent;
}
