package com.example.timesheet.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Table(name = "user_project_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "projectId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String username;

    private Long projectId;
}
