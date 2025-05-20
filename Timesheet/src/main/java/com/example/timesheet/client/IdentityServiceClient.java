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

    @GetMapping("/identity/users/{employeeCode}")
    ResponseEntity<UserIdentityDto> getUserByemployeeCode(@PathVariable("employeeCode") String employeeCode);

    @GetMapping("/identity/users/{employeeCode}/manager_role")
    ResponseEntity<Boolean> hasManagerRole(@PathVariable("employeeCode") String employeeCode, @RequestParam String roleName);

    @GetMapping("/identity/users")
    ResponseEntity<List<Map<String, String>>> getAllUsers();

    @GetMapping("/identity/users/manager/{managerCode}")
    ResponseEntity<List<UserIdentityDto>> getEmployeesUnderManager(@PathVariable String managerCode);
}
