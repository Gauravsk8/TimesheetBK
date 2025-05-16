package com.example.timesheet.Repository;

import com.example.timesheet.models.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostCenterRepository extends JpaRepository<CostCenter, String> {
    List<CostCenter> findBycostCenterManagerCodeIgnoreCase(String costCenterManagerCode);

    // Optional custom finder

}
