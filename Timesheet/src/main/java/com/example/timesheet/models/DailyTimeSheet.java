package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
public class DailyTimeSheet {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private String employeeId;
    private String employeeCode;

    private Timestamp date;

    @ManyToOne
    @JoinColumn(name = "weekly_time_sheet_id")
    private WeeklyTimeSheet weeklyTimeSheet;

    @OneToMany(mappedBy = "dailyTimeSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTimeEntry> projectTimeEntries = new ArrayList<>();

    private Long holiday;
    private Long leave;
    private Long ideal;
    private Long training;
    private Long totalHours;

    private boolean isIdealModifiedByManager = false;
    private boolean isHolidayModifiedByManager = false;
    private boolean isLeaveModifiedByManager = false;
    private boolean isTrainingModifiedByManager = false;
    private boolean isTotalHoursModifiedByManager = false;



}
