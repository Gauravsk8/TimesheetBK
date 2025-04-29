package com.example.timesheet.client;

import com.example.timesheet.config.FeignClientConfig;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.dto.response.UserAssignedRoleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "IdentityServiceClient",
        url = "${identity.service.url:http://localhost:8091}",
        configuration = FeignClientConfig.class
)
public interface IdentityServiceClient {

    @GetMapping("/timesheet/admin/User/employeeCode/{employeeCode}")
    ResponseEntity<UserIdentityDto> getUserByemployeeCode(@PathVariable("employeeCode") String employeeCode);

    @GetMapping("/timesheet/admin/User/Id/{id}")
    ResponseEntity<UserIdentityDto> getUserById(@PathVariable("id") String id);

    @GetMapping("/timesheet/{employeeCode}/getAssignedRoles")
    List<String> getAssignedRoles(@PathVariable("employeeCode") String employeeCode);


}
