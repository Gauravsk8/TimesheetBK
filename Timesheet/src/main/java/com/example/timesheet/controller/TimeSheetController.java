package com.example.timesheet.controller;

import com.example.timesheet.dto.request.DailyTimeSheetRequest;
import com.example.timesheet.dto.request.WeeklyTimeSheetRequest;
import com.example.timesheet.dto.response.WeeklyTimeSheetResponse;
import com.example.timesheet.service.TimeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimeSheetController {

    private final TimeSheetService timeSheetService;
    @PostMapping("/enterDailyTimeSheet")
    public ResponseEntity<String> enterDailyTimeSheet(@RequestBody DailyTimeSheetRequest dailyTimeSheetRequest){
        String result=timeSheetService.enterOrUpdateDailyTimeSheet(dailyTimeSheetRequest);
        return  ResponseEntity.ok().body(result);
    }

    @PostMapping("/enterWeeklyTimeSheet")
    public ResponseEntity<String> enterWeeklyTimeSheet(@RequestBody WeeklyTimeSheetRequest weeklyTimeSheetRequest){
        String result=timeSheetService.weeklyTimeSheetEntry(weeklyTimeSheetRequest);
        return  ResponseEntity.ok().body(result);
    }

    @GetMapping("/getWeeklyTimeSheetForAnEmployee")
    public WeeklyTimeSheetResponse getWeeklyTimeSheetForAnEmployee(@RequestParam("employeeCode") String employeeCode,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        System.out.println("Week start date in controller:"+weekStartDate);
        // Convert to Timestamp only if needed later
        // Manually set to 05:30:00
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return timeSheetService.getWeeklyTimeSheetForAnEmployee(employeeCode,startTs,endTs);
    }

    @GetMapping("/getWeeklyHoursSpent")
    public Long getWeeklyHoursSpent(@RequestParam("projectId") Long projectId,
                                    @RequestParam("employeeCode") String employeeCode,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return  timeSheetService.getWeeklyHoursSpent(projectId,employeeCode,startTs,endTs);
    }

    @GetMapping("/getWeeklyHoursSpent/{type}")
    public Long getWeeklyHoursSpent(@PathVariable("type") String type,
                                    @RequestParam("employeeCode") String employeeCode,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate){
        LocalTime fixedTime = LocalTime.of(5, 30);
        Timestamp startTs = Timestamp.valueOf(LocalDateTime.of(weekStartDate, fixedTime));
        Timestamp endTs = Timestamp.valueOf(LocalDateTime.of(weekEndDate, fixedTime));

        return timeSheetService.getWeeklyHoursSpentByType(employeeCode,type,startTs,endTs);
    }
}
