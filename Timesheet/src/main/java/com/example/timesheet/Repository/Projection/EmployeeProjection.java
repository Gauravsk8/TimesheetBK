package com.example.timesheet.Repository.Projection;

import java.util.List;

public interface EmployeeProjection {

    public Long getId();

    public String getFirstName();

    public String getLastName();

    public String getEmployeeNumber();

    public String getCity();

    public String getState();

    public String getCountry();

    public String getEmail();

    public List<RoleProjection> getRoles();

    public boolean isEmailVerified();
}