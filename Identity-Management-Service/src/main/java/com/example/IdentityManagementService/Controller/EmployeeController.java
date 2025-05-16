package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.Service.EmployeeService;
import com.example.IdentityManagementService.Service.ReportingManagerService;
import com.example.IdentityManagementService.Service.ServiceImpl.EmployeeServiceImpl;
import com.example.IdentityManagementService.Service.ServiceImpl.ReportingManagerServiceImpl;
import com.example.IdentityManagementService.dto.request.AssignRMRequest;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.constants.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
public class EmployeeController {

    private final ReportingManagerService employeeRmService;
    private final EmployeeService employeeService;


    //get EmployeeDetails form employeeCode
    @GetMapping("/user/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "tms:com", scope = "tms:com:get")
    public ResponseEntity<UserIdentityDto> getUserByEmployeeCode(@PathVariable String employeeCode) {
        UserIdentityDto dto = employeeService.getUserByEmployeeCodedb(employeeCode);
        return ResponseEntity.ok(dto);
    }

    //get My details
    @GetMapping("/user/my")
    @RequiresKeycloakAuthorization(resource = "idms:user", scope = "idms:user:get")
    public ResponseEntity<UserIdentityDto> getOwnProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String keycloakUserId = authentication.getName(); // This returns Keycloak UUID

        UserIdentityDto userProfile = employeeService.getUserByKeycloakUserId(keycloakUserId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/{employeeCode}/status")
    public ResponseEntity<String> updateActiveStatus(
            @PathVariable String employeeCode,
            @RequestParam boolean active
    ) {
        employeeService.updateActiveStatus(employeeCode, active);
        return ResponseEntity.ok(MessageConstants.USER_STATUS_UPDATED);
    }


    //get all employees
    @GetMapping("/users")
    @RequiresKeycloakAuthorization(resource = "manager:com", scope = "com:manager:get")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<Map<String, String>> users = employeeService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //get managerName for employeeCode
    @GetMapping("/user/manager_name/{employee_code}")
    @RequiresKeycloakAuthorization(resource = "idms:common", scope = "idms:user:get")
    public ResponseEntity<String> getManagerNameByEmployeeCode(
            @PathVariable String employeeCode
    ) {
        String managerName = employeeRmService.getManagerNameByEmployeeCode(employeeCode);
        return ResponseEntity.ok(managerName);
    }

    //assign reporting manager
    @PostMapping("/user/assign_manager")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:add")
    public ResponseEntity<String> assignReportingManager(@RequestBody AssignRMRequest request) {
        String response = employeeRmService.addReportingManagerToEmployee(
                request.getEmployeeCode(),
                request.getManagerCode()
        );
        return ResponseEntity.ok(response);
    }



}
