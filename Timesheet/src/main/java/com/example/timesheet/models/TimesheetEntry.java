package com.example.timesheet.models;


import com.example.timesheet.enums.EntryType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "timesheet_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "userId", "entryDate", "projectId", "entryType"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String userId;

    // Optional – null for default types (Holiday, Leave, etc.)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;

    @Column(nullable = false)
    private LocalDate entryDate;

    private Double hours;

    private String costCenter;
}
