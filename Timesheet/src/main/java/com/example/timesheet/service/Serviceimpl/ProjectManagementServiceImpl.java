package com.example.timesheet.service.Serviceimpl;

import com.example.common.constants.MessageConstants;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.ClientsRepository;
import com.example.timesheet.Repository.CostCenterRepository;
import com.example.timesheet.Repository.ProjectEmployeeRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.AssignEmployeesDto;
import com.example.timesheet.dto.request.ProjectDto;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.dto.response.ProjectEmployeeDto;
import com.example.timesheet.dto.response.ProjectResponseDto;
import com.example.timesheet.dto.response.ProjectWithEmployeesDto;
import com.example.timesheet.enums.Status;
import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.Clients;
import com.example.timesheet.models.CostCenter;
import com.example.timesheet.models.Project;
import com.example.timesheet.models.ProjectEmployee;
import com.example.timesheet.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectManagementServiceImpl implements ProjectManagementService {
    private final ClientsRepository clientsRepository;
    private final CostCenterRepository costCenterRepository;
    private final ProjectRepository projectRepository;
    private final IdentityServiceClient identityServiceClient;
    private final ProjectEmployeeRepository projectEmployeeRepository;

    private final String PROJECT_MANAGER_ROLE = "ProjectManager";

    public String createProject(ProjectDto dto) {

        Clients client = clientsRepository.findById(dto.getClientId())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId())
                ));

        CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.COST_CENTER_NOT_FOUND, dto.getCostCenterCode())
                ));


        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setClients(client);
        project.setCostCenter(costCenter);
        project.setProjectManagerCode(dto.getProjectManagerCode().toLowerCase());
        project.setAllocated_hours(dto.getAllocatedHours());
        project.setStatus(Status.ACTIVATE); // default status

        projectRepository.save(project);

        return MessageConstants.PROJECT_CREATED + dto.getTitle();
    }

    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProjectResponseDto getProjectByCode(String code) {
        Project project = projectRepository.findById(code.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, code)
                ));

        return mapToDto(project);
    }

    public String updateProject(String code, ProjectDto dto) {
        Project project = projectRepository.findById(code.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, code)
                ));

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setProjectManagerCode(dto.getProjectManagerCode().toLowerCase());
        project.setAllocated_hours(dto.getAllocatedHours());

        if (!project.getClients().getId().equals(dto.getClientId())) {
            Clients client = clientsRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId())
                    ));
            project.setClients(client);
        }

        if (!project.getCostCenter().getCostCenterCode().equalsIgnoreCase(dto.getCostCenterCode())) {
            CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode().toLowerCase())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.COST_CENTER_NOT_FOUND, dto.getCostCenterCode())
                    ));
            project.setCostCenter(costCenter);
        }

        projectRepository.save(project);
        return MessageConstants.PROJECT_UPDATE + project.getTitle();
    }

    public String assignEmployeesToProject(AssignEmployeesDto dto, String projectCode) {
        Project project = projectRepository.findById(projectCode.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        List<ProjectEmployee> assignments = dto.getEmployees().stream()
                .filter(emp -> {
                    ProjectEmployeeId id = new ProjectEmployeeId(projectCode.toLowerCase(), emp.getEmployeeCode().toLowerCase());
                    return !projectEmployeeRepository.existsById(id); // avoid duplicates
                })
                .map(emp -> {
                    ProjectEmployee pe = new ProjectEmployee();
                    ResponseEntity<UserIdentityDto> user;
                    try {
                        user = identityServiceClient.getUserByemployeeCode(emp.getEmployeeCode().toLowerCase());
                    } catch (Exception e) {
                        throw new TimeSheetException(errorCode.NOT_FOUND_ERROR, errorMessage.USER_NOT_FOUND + e.getMessage());
                    }
                    String EmployeeKeycloakId = user.getBody().getKeycloakUserId();
                    pe.setId(new ProjectEmployeeId(projectCode.toLowerCase(), emp.getEmployeeCode().toLowerCase()));
                    pe.setProject(project);
                    pe.setStartDate(emp.getStartDate());
                    pe.setEndDate(emp.getEndDate());
                    pe.setStatus(Status.ACTIVATE);
                    return pe;
                }).toList();

        projectEmployeeRepository.saveAll(assignments);
        return assignments.isEmpty()
                ? MessageConstants.EMPLOYEE_ALREADY_ASSINGNED
                : assignments.size() + MessageConstants.EMPLOYEE_ASSINGNED;
    }

    public List<ProjectEmployeeDto> getEmployeesByProject(String projectCode) {
        List<ProjectEmployee> entities = projectEmployeeRepository.findByProject_ProjectCodeIgnoreCase(projectCode.toLowerCase());

        return entities.stream().map(pe -> {
            ResponseEntity<UserIdentityDto> user = identityServiceClient.getUserByemployeeCode(pe.getId().getEmployeeCode().toLowerCase());

            return ProjectEmployeeDto.builder()
                    .employeeCode(pe.getId().getEmployeeCode())
                    .firstName(user.getBody().getFirstName())
                    .lastName(user.getBody().getLastName())
                    .startDate(pe.getStartDate())
                    .endDate(pe.getEndDate())
                    .status(pe.getStatus())
                    .build();
        }).toList();
    }

    public void removeEmployeeFromProject(String projectCode, String employeeCode) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode.toLowerCase(), employeeCode.toLowerCase());

        if (!projectEmployeeRepository.existsById(id)) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,  // Assuming this is the error code
                    String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)  // Assuming this error message exists
            );
        }

        projectEmployeeRepository.deleteById(id);
    }

    public ProjectWithEmployeesDto getProjectWithEmployees(String projectCode) {
        Project project = projectRepository.findById(projectCode.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        List<ProjectEmployeeDto> employees = getEmployeesByProject(projectCode.toLowerCase());

        return ProjectWithEmployeesDto.builder()
                .projectCode(project.getProjectCode())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .costCenterCode(project.getCostCenter().getCostCenterCode())
                .clientName(project.getClients().getName())
                .projectManagerCode(project.getProjectManagerCode())
                .employees(employees)
                .build();

    }
    public void deleteProject(String projectCode) {
        Project project = projectRepository.findById(projectCode.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        projectRepository.delete(project);
    }

    public String updateEmployeeStatus(String projectCode, String employeeCode, String newStatus) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode.toLowerCase(), employeeCode.toLowerCase());

        ProjectEmployee projectEmployee = projectEmployeeRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));

        Status statusEnum;
        try {
            statusEnum = Status.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TimeSheetException(errorCode.NOT_FOUND_ERROR, errorMessage.STATUS_NOT_FOUND + newStatus);
        }

        projectEmployee.setStatus(statusEnum);  // <-- set the new status
        projectEmployeeRepository.save(projectEmployee);

        return String.format(MessageConstants.PROJECT_STATUS_UPDATED, employeeCode, projectCode);
    }


    public String updateEmployee(String projectCode, String employeeCode, AssignEmployeesDto.EmployeeAssignment dto) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode.toLowerCase(), employeeCode.toLowerCase());

        ProjectEmployee projectEmployee = projectEmployeeRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));
        projectEmployee.setStartDate(dto.getStartDate());
        projectEmployee.setEndDate(dto.getEndDate());
        projectEmployeeRepository.save(projectEmployee);
        return String.format(MessageConstants.PROJECT_STATUS_UPDATED, employeeCode, projectCode);
    }

    public List<ProjectDto> getProjectsByEmployeeCode(String employeeCode) {
        List<ProjectEmployee> assignments = projectEmployeeRepository.findByIdEmployeeCodeIgnoreCase(employeeCode);

        return assignments.stream()
                .map(pe -> {
                    Project project = pe.getProject();

                    ProjectDto dto = new ProjectDto();
                    dto.setProjectCode(project.getProjectCode());
                    dto.setTitle(project.getTitle());
                    dto.setDescription(project.getDescription());
                    dto.setStartDate(project.getStartDate());
                    dto.setEndDate(project.getEndDate());
                    dto.setProjectManagerCode(project.getProjectManagerCode());
                    dto.setAllocatedHours(project.getAllocated_hours());
                    dto.setCostCenterCode(project.getCostCenter().getCostCenterCode());
                    return dto;
                })
                .toList();
    }


    private ProjectResponseDto mapToResponse(Project project) {
        return ProjectResponseDto.builder()
                .projectCode(project.getProjectCode())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .clientName(project.getClients().getName())
                .costCenterCode(project.getCostCenter().getCostCenterCode())
                .projectManagerCode(project.getProjectManagerCode())
                .allocatedHours(project.getAllocated_hours())
                .build();
    }
    public List<Map<String, String>> getUnassignedUsersForProject(String projectCode) {
        Project project = projectRepository.findById(projectCode.toLowerCase())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode)
                ));

        Set<String> assignedEmployeeCodes = project.getProjectEmployees().stream()
                .map(pe -> pe.getId().getEmployeeCode())
                .collect(Collectors.toSet());

        List<Map<String, String>> allUsers = identityServiceClient.getAllUsers().getBody();

        return allUsers.stream()
                .filter(user -> !assignedEmployeeCodes.contains(user.get("employeeCode")))
                .collect(Collectors.toList());
    }

    public List<ProjectWithEmployeesDto> getProjectsWithEmployeesUnderManager(String projectManagerCode) {
        List<Project> projects = projectRepository.findByprojectManagerCodeIgnoreCase(projectManagerCode);

        return projects.stream().map(project -> {

            List<ProjectEmployeeDto> employeeDtos = project.getProjectEmployees().stream().map(pe -> {
                String empCode = pe.getId().getEmployeeCode();

                // Call identity service for user details
                ResponseEntity<UserIdentityDto> userResponse = identityServiceClient.getUserByemployeeCode(empCode);
                UserIdentityDto user = userResponse.getBody();

                return ProjectEmployeeDto.builder()
                        .employeeCode(empCode)
                        .firstName(user != null ? user.getFirstName() : null)
                        .lastName(user != null ? user.getLastName() : null)
                        .startDate(pe.getStartDate())
                        .endDate(pe.getEndDate())
                        .status(pe.getStatus())
                        .build();
            }).collect(Collectors.toList());

            return ProjectWithEmployeesDto.builder()
                    .projectCode(project.getProjectCode())
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .projectManagerCode(project.getProjectManagerCode())
                    .employees(employeeDtos)
                    .build();

        }).collect(Collectors.toList());
    }


    // Mapping function from Project to ProjectResponseDto
    private ProjectResponseDto mapToDto(Project project) {
        return ProjectResponseDto.builder()
                .projectCode(project.getProjectCode())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .clientName(project.getClients().getName())
                .costCenterCode(project.getCostCenter().getCostCenterCode())
                .projectManagerCode(project.getProjectManagerCode())
                .allocatedHours(project.getAllocated_hours())
                .status(project.getStatus())
                .build();
    }
}