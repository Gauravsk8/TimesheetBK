package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
public class WeeklyTimeSheet {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private String employeeId;
    private String employeeCode;

    private Timestamp weekStartDate;


    @OneToMany(mappedBy = "weeklyTimeSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyTimeSheet> dailySheets = new ArrayList<>();

    private Long totalWorkingHours; // Computed field: Sum of project + training hours
    private Long totalIdleHours;    // Sum of idle + leave + holiday


}
