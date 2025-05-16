package com.example.timesheet.models;

import com.example.timesheet.enums.Status;
import com.example.timesheet.keys.ProjectEmployeeId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.sql.Timestamp;

@Entity
@Table(name = "project_employee")
@Getter
@Setter
@NoArgsConstructor
public class ProjectEmployee {

    @EmbeddedId
    private ProjectEmployeeId id = new ProjectEmployeeId();

    @ManyToOne
    @MapsId("projectCode") // Links part of the composite key to Project
    @JoinColumn(name = "projectCode")
    private Project project;

    private String role_in_project;


    @Column(name = "start_date")
    private Timestamp startDate;  // When assignment begins

    @Column(name = "end_date")
    private Timestamp endDate;    // When assignment ends (nullable)

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

}
