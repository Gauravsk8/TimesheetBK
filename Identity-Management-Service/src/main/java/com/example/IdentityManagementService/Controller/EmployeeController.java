package com.example.IdentityManagementService.Controller;

import com.example.IdentityManagementService.Service.EmployeeService;
import com.example.IdentityManagementService.Service.ReportingManagerService;
import com.example.IdentityManagementService.Service.ServiceImpl.EmployeeServiceImpl;
import com.example.IdentityManagementService.Service.ServiceImpl.ReportingManagerServiceImpl;
import com.example.IdentityManagementService.dto.request.AssignRMRequest;
import com.example.IdentityManagementService.dto.request.Response.UserResponseDto;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.constants.MessageConstants;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
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
    @GetMapping("/users/{employeeCode}")
    @RequiresKeycloakAuthorization(resource = "tms:com", scope = "tms:com:get")
    public ResponseEntity<UserIdentityDto> getUserByEmployeeCode(@PathVariable String employeeCode) {
        UserIdentityDto dto = employeeService.getUserByEmployeeCodedb(employeeCode);
        return ResponseEntity.ok(dto);
    }

    //get My details
    @GetMapping("/users/my")
    @RequiresKeycloakAuthorization(resource = "idms:user", scope = "idms:user:get")
    public ResponseEntity<UserIdentityDto> getOwnProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String keycloakUserId = authentication.getName(); // This returns Keycloak UUID

        UserIdentityDto userProfile = employeeService.getUserByKeycloakUserId(keycloakUserId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("users/{employeeCode}/status")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:update")
    public ResponseEntity<String> updateActiveStatus(
            @PathVariable String employeeCode,
            @RequestParam boolean active
    ) {
        employeeService.updateActiveStatus(employeeCode, active);
        return ResponseEntity.ok(MessageConstants.USER_STATUS_UPDATED);
    }

    @GetMapping("/users")
    @RequiresKeycloakAuthorization(resource = "manager:com", scope = "com:manager:get")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<Map<String, String>> users = employeeService.getAllUsersList();
        return ResponseEntity.ok(users);
    }


    //get all employees
    @PostMapping("/users/Page")
    @RequiresKeycloakAuthorization(resource = "manager:com", scope = "com:manager:get")
    public ResponseEntity<PagedResponse<UserResponseDto>> getAllUsersPaged(
            @RequestBody PageRequestDto pageRequestDto) {
        return ResponseEntity.ok(employeeService.getAllUsers(pageRequestDto));
    }

    //get managerName for employeeCode
    @GetMapping("/users/{employee_code}/manager")
    @RequiresKeycloakAuthorization(resource = "tms:com", scope = "tms:com:get")
    public ResponseEntity<String> getManagerNameByEmployeeCode(
            @PathVariable String employee_code
    ) {
        String managerName = employeeRmService.getManagerNameByEmployeeCode(employee_code);
        return ResponseEntity.ok(managerName);
    }

    //assign reporting manager
    @PostMapping("/users/manager")
    @RequiresKeycloakAuthorization(resource = "idms:admin", scope = "idms:user:add")
    public ResponseEntity<String> assignReportingManager(@RequestBody AssignRMRequest request) {
        String response = employeeRmService.addReportingManagerToEmployee(
                request.getEmployeeCode(),
                request.getManagerCode()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("users/manager/{managerCode}")
    @RequiresKeycloakAuthorization(resource = "idms:adminrm", scope = "idms:user:get")
    public ResponseEntity<List<UserIdentityDto>> getEmployeesUnderManager(
            @PathVariable String managerCode) {
        List<UserIdentityDto> employees = employeeService.getActiveEmployeesUnderManager(managerCode);

        return ResponseEntity.ok(employees);

    }

}
