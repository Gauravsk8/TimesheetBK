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
public class DailyTimeSheet {

    @EmbeddedId
    private DailyTimeSheetId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectCode", referencedColumnName = "projectCode", insertable = false, updatable = false)
    private Project project;

    @Column(nullable = false)
    private Double hoursSpent;

    @Column(length = 1000)
    private String description;

    @Column
    private Boolean modifiedByManager = false;


}
