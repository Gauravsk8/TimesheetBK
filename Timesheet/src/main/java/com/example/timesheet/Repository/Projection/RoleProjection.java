package com.example.timesheet.Repository.Projection;

import java.util.Set;

public interface RoleProjection {
    public Long getId();

    public String getName();

    public Set<PrivilegeProjection> getPrivileges();

}
