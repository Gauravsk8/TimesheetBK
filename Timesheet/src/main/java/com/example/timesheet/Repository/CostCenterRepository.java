package com.example.timesheet.Repository;

import com.example.timesheet.models.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCenterRepository extends JpaRepository<CostCenter, String> {

    // Optional custom finder
    boolean existsByManagerCode(String managerCOde);

}
