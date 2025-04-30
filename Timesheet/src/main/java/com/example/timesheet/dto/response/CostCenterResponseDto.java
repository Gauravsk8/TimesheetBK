package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CostCenterResponseDto {

    private String code;
    private String name;
    private String description;
    private String managerCode;

}
