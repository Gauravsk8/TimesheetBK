package com.example.timesheet.Repository;

import com.example.timesheet.models.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostCenterRepository extends JpaRepository<CostCenter, String> {

    List<CostCenter> findByCostCenterManagerCodeIgnoreCaseAndIsActiveTrue(String costCenterManagerCode);

    // ✅ Fixed method name
    List<CostCenter> findByIsActiveTrue();

    Optional<CostCenter> findByCostCenterCodeAndIsActiveTrue(String costCenterCode);
}

