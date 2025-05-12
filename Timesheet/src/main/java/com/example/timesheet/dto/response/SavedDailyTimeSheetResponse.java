package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SavedDailyTimeSheetResponse {
    private List<DailyTimeSheetResponse> dailyTimeSheetResponses = new ArrayList<>();

}
