package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "weekly_timesheet")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class WeeklyTimeSheet extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employeeId", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String status;

    @Column(name = "total_week_hours", nullable = false)
    private Double totalWeekHours = 0.0;

    @OneToMany(mappedBy = "weeklyTimeSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DailyTimeSheet> dailyTimeSheets = new LinkedHashSet<>();

    @OneToOne(mappedBy = "weeklyTimesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

}
