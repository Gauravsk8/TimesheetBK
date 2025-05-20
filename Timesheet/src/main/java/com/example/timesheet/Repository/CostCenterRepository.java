package com.example.timesheet.Repository;

import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.timesheet.dto.response.CostCenterResponseDto;
import com.example.timesheet.models.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CostCenterRepository extends JpaRepository<CostCenter, String>, JpaSpecificationExecutor<CostCenter> {

    List<CostCenter> findByCostCenterManagerCodeIgnoreCaseAndIsActiveTrue(String costCenterManagerCode);

    // ✅ Fixed method name
    List<CostCenter> findByIsActiveTrue();

    Optional<CostCenter> findByCostCenterCodeAndIsActiveTrue(String costCenterCode);
}

