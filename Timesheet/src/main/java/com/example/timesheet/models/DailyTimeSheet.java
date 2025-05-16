package com.example.timesheet.models;

import com.example.timesheet.keys.DailyTimeSheetId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "daily_time_sheet",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timesheet_business_key",
                        columnNames = {
                                "employeeCode",
                                "timesheet_year",
                                "timesheet_month",
                                "workDate",
                                "entryType",
                                "projectCode"
                        }
                )
        }
)
public class DailyTimeSheet {

    @EmbeddedId
    private DailyTimeSheetId id;

    @Column(name = "projectCode", nullable = true)
    private String projectCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "projectCode", referencedColumnName = "projectCode", insertable = false, updatable = false)
    private Project project;

    @Column(nullable = false)
    private Double hoursSpent;

    @Column(length = 1000)
    private String description;

    @Column
    private Boolean modifiedByManager = false;


}
