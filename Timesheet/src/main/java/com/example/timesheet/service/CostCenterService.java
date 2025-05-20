package com.example.timesheet.service;

import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.CostCenterDto;
import com.example.timesheet.dto.response.CostCenterResponseDto;
import com.example.timesheet.dto.response.ProjectResponseDto;

import java.util.List;

public interface CostCenterService {
    String createCostCenter(CostCenterDto dto) throws TimeSheetException;
    PagedResponse<CostCenterResponseDto> getAllCostCenters(PageRequestDto pageRequestDto);
    CostCenterResponseDto getCostCenterByCode(String costCenterCode) throws TimeSheetException;
    String updateCostCenter(String costCenterCode, CostCenterDto dto) throws TimeSheetException;
    String updateCostCenterStatus(String costCenterCode, boolean newStatus) throws TimeSheetException;
    List<CostCenterResponseDto> getAllCostCentersUnderManager(String costCenterManagerCode);
    List<ProjectResponseDto> getProjectsByCostCenterCode(String costCenterCode);
}