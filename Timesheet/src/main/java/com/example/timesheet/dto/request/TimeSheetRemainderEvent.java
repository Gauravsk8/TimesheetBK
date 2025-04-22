package com.example.timesheet.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
@Getter
@Setter
@RequiredArgsConstructor

public class TimeSheetRemainderEvent {

    private String email;
    private String firstName;
    private String lastName;
    private LocalDate weekEndingDate;

    @JsonCreator
    public TimeSheetRemainderEvent(@JsonProperty("email") String email,
                                   @JsonProperty("firstName") String firstName,
                                   @JsonProperty("lastName") String lastName,
                                   @JsonFormat(pattern = "yyyy-MM-dd") LocalDate weekEndingDate) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.weekEndingDate = weekEndingDate;
    }

    // getters and setters
}
