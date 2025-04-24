package com.example.timesheet.dto.response;

import com.example.timesheet.models.ProjectTimeEntry;
import com.example.timesheet.models.WeeklyTimeSheet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class DailyTimeSheetResponse {
    protected Long id;
    private String employeeId;
    private String employeeCode;
    private Timestamp date;
    private List<ProjectTimeSheetEntryResponse> projectTimeSheetEntryResponses = new ArrayList<>();
    private Long holiday;
    private Long leave;
    private Long ideal;
    private Long training;
    private Long totalHours;


}
