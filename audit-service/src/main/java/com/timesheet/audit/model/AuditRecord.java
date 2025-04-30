package com.timesheet.audit.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String serviceName;
    private String actor;
    private String action;
    private String details;
    private Instant timestamp;
}