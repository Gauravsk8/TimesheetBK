package com.example.timesheet.client;

import com.example.timesheet.config.FeignClientConfig;
import com.example.timesheet.dto.request.UserIdentityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "IdentityServiceClient",
        url = "${identity.service.url:http://localhost:8091}",
        configuration = FeignClientConfig.class
)
public interface IdentityServiceClient {

    @GetMapping("/timesheet/employeeCode/{employeeCode}")
    ResponseEntity<UserIdentityDto> getUserByUsername(@PathVariable("employeeCode") String employeeCode);

    @GetMapping("/timesheet/Id/{id}")
    ResponseEntity<UserIdentityDto> getUserById(@PathVariable("id") String id);

}
