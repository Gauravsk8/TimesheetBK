package com.example.IdentityManagementService.Service.ServiceImpl;

import com.example.IdentityManagementService.Repository.EmployeeRepository;
import com.example.IdentityManagementService.Service.EmployeeService;
import com.example.IdentityManagementService.dto.request.EmployeeRequestDto;
import com.example.IdentityManagementService.dto.request.Response.UserResponseDto;
import com.example.IdentityManagementService.dto.request.UserIdentityDto;
import com.example.IdentityManagementService.exceptions.TimesheetException;
import com.example.IdentityManagementService.model.Employee;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.common.exceptions.TimeSheetException;
import com.example.common.utils.FilterSpecificationBuilder;
import com.example.common.utils.SortUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.common.constants.errorCode.INTERNAL_SERVER_ERROR;
import static com.example.common.constants.errorCode.NOT_FOUND_ERROR;
import static com.example.common.constants.errorMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserIdentityDto getUserByEmployeeCodedb(String employeeCode) {
        Employee user = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, USER_NOT_FOUND + employeeCode));

        UserIdentityDto dto = new UserIdentityDto();
        dto.setKeycloakUserId(user.getKeycloakUserId());
        dto.setEmployeeCode(user.getEmployeeCode());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setManagerCode(user.getManagerCode());
        dto.setEmployeeType(user.getEmployeeType());

        return dto;
    }

    public void updateActiveStatus(String employeeCode, boolean isActive) {
        Employee employee = employeeRepository.findById(employeeCode)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, USER_NOT_FOUND));

        employee.setActive(isActive);
        employeeRepository.save(employee);
    }

    @Override
    public UserIdentityDto getUserByKeycloakUserId(String keycloakUserId) {
        Employee employee = employeeRepository.findByKeycloakUserIdAndIsActiveTrue(keycloakUserId)
                .orElseThrow(() -> new TimesheetException(NOT_FOUND_ERROR, USER_NOT_FOUND + keycloakUserId));

        UserIdentityDto dto = new UserIdentityDto();
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setEmployeeType(employee.getEmployeeType());
        dto.setManagerCode(employee.getManagerCode());
        return dto;
    }


    @Override
    public PagedResponse<UserResponseDto> getAllUsers(PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(
                pageRequestDto.getPage(),
                pageRequestDto.getSize(),
                SortUtil.getSort(pageRequestDto.getSort())
        );

        Specification<Employee> spec = new FilterSpecificationBuilder<Employee>()
                .build(pageRequestDto.getFilter());

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);

        if (employeePage.isEmpty()) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,
                    errorMessage.NO_ACTIVE_USERS_FOUND
            );
        }

        List<UserResponseDto> content = employeePage.getContent().stream()
                .map(emp -> new UserResponseDto(
                        emp.getEmployeeCode(),
                        emp.getFirstName(),
                        emp.getLastName(),
                        emp.getEmail(),
                        emp.getManagerCode(),
                        emp.getEmployeeType()
                )).toList();

        return new PagedResponse<>(
                content,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements()
        );
    }




    @Override
    public List<UserIdentityDto> getActiveEmployeesUnderManager(String managerCode) {
        List<Employee> employees = employeeRepository.findByManagerCodeAndIsActiveTrue(managerCode);
        return mapToUserIdentityDtos(employees);
    }

    private List<UserIdentityDto> mapToUserIdentityDtos(List<Employee> employees) {
        return employees.stream()
                .map(this::mapToUserIdentityDto)
                .collect(Collectors.toList());
    }

    private UserIdentityDto mapToUserIdentityDto(Employee employee) {
        return UserIdentityDto.builder()
                .keycloakUserId(employee.getKeycloakUserId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .employeeType(employee.getEmployeeType())
                .managerCode(employee.getManagerCode())
                .build();
    }


}
