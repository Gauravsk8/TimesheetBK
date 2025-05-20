package com.example.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private List<FilterRequest> filter;
    private List<SortRequest> sort;

    // Getters and Setters
}
