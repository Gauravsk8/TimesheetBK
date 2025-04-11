package com.example.timesheet.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class Project extends BaseEntity{

    private String name;

    private Timestamp start_date;

    private Time end_date;

    @ManyToOne
    @JoinColumn(name="client_id",nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name="employeeId",referencedColumnName = "employeeId",nullable = false)
    private Employee manager;

    @ManyToMany
    @JoinTable(
            name = "project_employee",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "employeeId")
    )
    private Set<Employee> assignedEmployees=new LinkedHashSet<>();
}
