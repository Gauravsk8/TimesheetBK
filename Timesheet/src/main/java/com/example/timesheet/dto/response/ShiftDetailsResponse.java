package com.example.timesheet.dto.response;


import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ShiftDetailsResponse {

    private DayOfWeek startDay;
    private DayOfWeek endDay;
    private LocalTime startTime;
    private LocalTime endTime;

}
