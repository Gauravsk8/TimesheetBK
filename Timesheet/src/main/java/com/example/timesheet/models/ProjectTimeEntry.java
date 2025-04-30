package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
public class ProjectTimeEntry{

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @ManyToOne
    @JoinColumn(name="daily_time_sheet_id")
    private DailyTimeSheet dailyTimeSheet;
    private Long projectCode;
    private Long totalHoursSpent;

    private boolean isTotalHoursSpentOnProjectModifiedByManager = false;

}
