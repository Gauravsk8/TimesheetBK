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
public class Review extends BaseEntity{
    @OneToOne
    @JoinColumn(name = "weekly_timesheet_id", nullable = false)
    private WeeklyTimeSheet weeklyTimesheet;

    @ManyToOne
    @JoinColumn(name = "employeeId", nullable = false)
    private Employee reviewer; // Only HR or Manager can review

    @Column(nullable = false)
    private boolean finalApproval;
}
