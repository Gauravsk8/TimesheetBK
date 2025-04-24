package com.example.timesheet.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class Project {

    @Id
    private String projectCode;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String owner;

    @Column
    private Timestamp start_date;

    @Column
    private Timestamp end_date;

    @ManyToOne
    @JoinColumn(name="clients_id",nullable = false)
    private Clients clients;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private CostCenter costCenter;

    @Column
    private String managerId;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectEmployee> projectEmployees = new HashSet<>();

    @Column
    private String allocated_hours;

}