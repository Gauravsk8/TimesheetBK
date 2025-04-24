package com.example.timesheet.dto.request;


import lombok.Data;

@Data
public class ClientDto {
    private String name;
    private String contactPerson;
    private String contactEmail;
    private String address;
}
