package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
public class DailyTimeSheet extends BaseEntity{

    @OneToOne
    @JoinColumn(name="employeeId",nullable = false)
    private Employee employee;

    @OneToOne
    @JoinColumn(name="weekly_time_sheet_id")
    private WeeklyTimeSheet weeklyTimeSheet;

    @OneToMany
    @JoinColumn(name="daily_time_sheet_id")
    private Set<TimeSheetEntry> timeSheetEntryList=new LinkedHashSet<>();

    @Column(nullable = false)
    private Long totalWorkedHours;

    private Long totalIdleHours;

}
