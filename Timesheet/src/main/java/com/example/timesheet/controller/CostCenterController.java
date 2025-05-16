package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.CostCenterDto;
import com.example.timesheet.dto.response.CostCenterResponseDto;
import com.example.timesheet.dto.response.ProjectResponseDto;
import com.example.timesheet.service.CostCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class CostCenterController {

    private final CostCenterService costCenterService;


    @PostMapping("/costCenter")
    @RequiresKeycloakAuthorization(resource = "tms:adminccm", scope = "tms:costcenter:add")
    public ResponseEntity<String> createCostCenter(@RequestBody CostCenterDto dto) {
        return ResponseEntity.ok(costCenterService.createCostCenter(dto));
    }

    @GetMapping("/costCenters")
    @RequiresKeycloakAuthorization(resource = "tms:adminccmpm", scope = "tms:costcenter:get")
    public ResponseEntity<List<CostCenterResponseDto>> getAllCostCenters() {
        return ResponseEntity.ok(costCenterService.getAllCostCenters());
    }

    @GetMapping("/costCenter/{costCenterManagerCode}")
    @RequiresKeycloakAuthorization(resource = "tms:ccm", scope = "tms:costcenter:get")
    public ResponseEntity<List<CostCenterResponseDto>> getAllCostCentersUnderManager(@PathVariable String costCenterManagerCode) {
        return ResponseEntity.ok(costCenterService.getAllCostCentersUnderManager(costCenterManagerCode));
    }

    @GetMapping("/costCenter/{costCenterCode}")
    @RequiresKeycloakAuthorization(resource = "tms:adminccm", scope = "tms:costcenter:get")
    public ResponseEntity<CostCenterResponseDto> getCostCenterByCode(@PathVariable String costCenterCode) {
        try {
            return ResponseEntity.ok(costCenterService.getCostCenterByCode(costCenterCode));
        }  catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PutMapping("/costCenter/{costCenterCode}")
    @RequiresKeycloakAuthorization(resource = "tms:adminccm", scope = "tms:costcenter:update")
    public ResponseEntity<String> updateCostCenter(@PathVariable String costCenterCode, @RequestBody CostCenterDto dto) {
        try {
            return ResponseEntity.ok(costCenterService.updateCostCenter(costCenterCode, dto));
        }  catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }

    @PutMapping("/costCenter/{costCenterCode}/status")
    @RequiresKeycloakAuthorization(resource = "tms:adminccm", scope = "tms:costcenter:update")
    public ResponseEntity<String> updatecostCenterStatus(
            @PathVariable String costCenterCode,
            @RequestParam String newStatus) {

        String response = costCenterService.updateCostCenterStatus(costCenterCode, newStatus);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/costCenter/{costCenterCode}/Projects")
    @RequiresKeycloakAuthorization(resource = "tms:adminccm", scope = "tms:project:get")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByCostCenter(@PathVariable("code") String costCenterCode) {
        List<ProjectResponseDto> projects = costCenterService.getProjectsByCostCenterCode(costCenterCode);
        return ResponseEntity.ok(projects);
    }

}
