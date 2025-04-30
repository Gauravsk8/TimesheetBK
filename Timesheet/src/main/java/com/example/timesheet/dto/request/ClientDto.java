package com.example.timesheet.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientDto {

    private String name;
    private String contactPerson;
    private String contactEmail;
    private String address;
}
