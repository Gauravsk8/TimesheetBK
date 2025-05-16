package com.example.timesheet.models;

import com.example.timesheet.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Clients {

    @Id
    @Column(name = "client_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactPerson;

    private String contactEmail;

    private String address;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
}
