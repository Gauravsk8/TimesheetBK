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

    @GetMapping("/timesheet/get-user-by-username/{username}")
    ResponseEntity<UserIdentityDto> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/timesheet/get-user-by-id/{id}")
    ResponseEntity<UserIdentityDto> getUserById(@PathVariable("id") String id);

}
