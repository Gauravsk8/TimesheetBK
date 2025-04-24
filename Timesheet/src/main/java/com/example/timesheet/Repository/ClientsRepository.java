package com.example.timesheet.Repository;

import com.example.timesheet.models.Clients;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientsRepository extends JpaRepository<Clients, Long> {
    // Optional custom finder
    boolean existsByContactEmail(String contactEmail);
}
