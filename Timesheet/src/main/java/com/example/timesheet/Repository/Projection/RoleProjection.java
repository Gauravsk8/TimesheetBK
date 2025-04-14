package com.example.timesheet.Repository.Projection;

import java.util.Set;

public interface RoleProjection {
     Long getId();

     String getName();

     Set<PrivilegeProjection> getPrivileges();

}
