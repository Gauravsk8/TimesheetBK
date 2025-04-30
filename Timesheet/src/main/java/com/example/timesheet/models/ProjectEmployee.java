package com.example.timesheet.models;

import com.example.timesheet.enums.EmployeeStatus;
import com.example.timesheet.keys.ProjectEmployeeId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.sql.Timestamp;

@Entity
@Table(name = "project_employee")
@Getter
@Setter
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class ProjectEmployee {

    @EmbeddedId
    private ProjectEmployeeId id = new ProjectEmployeeId();

    @ManyToOne
    @MapsId("projectCode") // Links part of the composite key to Project
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "employee_keycloak_Id")
    private String employeeKeycloakId;

    @Column(name = "start_date")
    private Timestamp startDate;  // When assignment begins

    @Column(name = "end_date")
    private Timestamp endDate;    // When assignment ends (nullable)

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

}
