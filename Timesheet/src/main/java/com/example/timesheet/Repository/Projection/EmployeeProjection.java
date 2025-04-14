package com.example.timesheet.Repository.Projection;

import java.util.List;

public interface EmployeeProjection {

     Long getId();

     String getFirstName();

     String getLastName();

     String getEmployeeNumber();

     String getCity();

     String getState();

     String getCountry();

     String getEmail();

     List<RoleProjection> getRoles();

     boolean isEmailVerified();
}