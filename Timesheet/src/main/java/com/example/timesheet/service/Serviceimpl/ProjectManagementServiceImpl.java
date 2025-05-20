package com.example.timesheet.service.Serviceimpl;

import com.example.common.constants.MessageConstants;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.common.exceptions.TimeSheetException;
import com.example.common.utils.FilterSpecificationBuilder;
import com.example.common.utils.SortUtil;
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
import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.Clients;
import com.example.timesheet.models.CostCenter;
import com.example.timesheet.models.Project;
import com.example.timesheet.models.ProjectEmployee;
import com.example.timesheet.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
    @Override
    public String createProject(ProjectDto dto) {

        Clients client = clientsRepository.findByIdAndIsActiveTrue(dto.getClientId())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId())
                ));

        CostCenter costCenter = costCenterRepository.findByCostCenterCodeAndIsActiveTrue(dto.getCostCenterCode())
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
        project.setProjectManagerCode(dto.getProjectManagerCode());
        project.setAllocated_hours(dto.getAllocatedHours());

        projectRepository.save(project);

        return MessageConstants.PROJECT_CREATED + dto.getTitle();
    }

    @Override
    public PagedResponse<ProjectResponseDto> getAllProjects(PageRequestDto pageRequestDto) {
        // Create Pageable from page, size, and sort
        Pageable pageable = PageRequest.of(
                pageRequestDto.getPage(),
                pageRequestDto.getSize(),
                SortUtil.getSort(pageRequestDto.getSort())
        );

        // Create filter spec from the provided filter criteria
        Specification<Project> spec = new FilterSpecificationBuilder<Project>()
                .build(pageRequestDto.getFilter());

        // Fetch paginated + filtered result from DB
        Page<Project> projectPage = projectRepository.findAll(spec, pageable);

        // Optional: throw exception if empty
        if (projectPage.isEmpty()) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,
                    errorMessage.NO_ACTIVE_PROJECTS_FOUND
            );
        }

        // Map to response DTO
        List<ProjectResponseDto> content = projectPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Return PagedResponse
        return new PagedResponse<>(
                content,
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements()
        );
    }

    @Override
    public ProjectResponseDto getProjectByCode(String code) {
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, code)
                ));
        return mapToDto(project);
    }

    @Override
    public String updateProject(String code, ProjectDto dto) {
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, code)
                ));

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setProjectManagerCode(dto.getProjectManagerCode());
        project.setAllocated_hours(dto.getAllocatedHours());

        if (!project.getClients().getId().equals(dto.getClientId())) {
            Clients client = clientsRepository.findByIdAndIsActiveTrue(dto.getClientId())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId())
                    ));
            project.setClients(client);
        }

        if (!project.getCostCenter().getCostCenterCode().equalsIgnoreCase(dto.getCostCenterCode())) {
            CostCenter costCenter = costCenterRepository.findByCostCenterCodeAndIsActiveTrue(dto.getCostCenterCode())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR,
                            String.format(errorMessage.COST_CENTER_NOT_FOUND, dto.getCostCenterCode())
                    ));
            project.setCostCenter(costCenter);
        }

        projectRepository.save(project);
        return MessageConstants.PROJECT_UPDATE + project.getTitle();
    }

    @Override
    public String updateProjectStatus(String projectCode, boolean active) throws TimeSheetException {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode)));


        project.setActive(active);
        Project savedProject = projectRepository.save(project);
        return String.format(MessageConstants.PROJECT_STATUS_UPDATED, savedProject.getTitle());
    }

    @Override
    public String assignEmployeesToProject(AssignEmployeesDto dto, String projectCode) {
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));
        List<ProjectEmployee> assignments = dto.getEmployees().stream()
                .filter(emp -> {
                    ProjectEmployeeId id = new ProjectEmployeeId(projectCode, emp.getEmployeeCode());
                    return !projectEmployeeRepository.existsByIdAndIsActiveTrue(id); // avoid duplicates
                })
                .map(emp -> {
                    ProjectEmployee pe = new ProjectEmployee();
                    ResponseEntity<UserIdentityDto> user;
                    try {
                        user = identityServiceClient.getUserByemployeeCode(emp.getEmployeeCode());
                    } catch (Exception e) {
                        throw new TimeSheetException(errorCode.NOT_FOUND_ERROR, errorMessage.USER_NOT_FOUND + e.getMessage());
                    }
                    String EmployeeKeycloakId = user.getBody().getKeycloakUserId();
                    pe.setId(new ProjectEmployeeId(projectCode, emp.getEmployeeCode()));
                    pe.setProject(project);
                    pe.setStartDate(project.getStartDate());
                    pe.setEndDate(project.getEndDate());
                    pe.setRole_in_project(emp.getRole_in_project());
                    return pe;
                }).toList();

        projectEmployeeRepository.saveAll(assignments);
        return assignments.isEmpty()
                ? MessageConstants.EMPLOYEE_ALREADY_ASSINGNED
                : assignments.size() + MessageConstants.EMPLOYEE_ASSINGNED;
    }

    @Override
    public List<ProjectEmployeeDto> getEmployeesByProject(String projectCode) {
        List<ProjectEmployee> entities = projectEmployeeRepository.findByProject_ProjectCodeIgnoreCaseAndIsActiveTrue(projectCode);

        return entities.stream().map(pe -> {
            ResponseEntity<UserIdentityDto> user = identityServiceClient.getUserByemployeeCode(pe.getId().getEmployeeCode());

            return ProjectEmployeeDto.builder()
                    .employeeCode(pe.getId().getEmployeeCode())
                    .firstName(user.getBody().getFirstName())
                    .lastName(user.getBody().getLastName())
                    .startDate(pe.getStartDate())
                    .endDate(pe.getEndDate())
                    .isActive(pe.isActive())
                    .build();
        }).toList();
    }

    public void removeEmployeeFromProject(String projectCode, String employeeCode) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        if (!projectEmployeeRepository.existsByIdAndIsActiveTrue(id)) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,  // Assuming this is the error code
                    String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)  // Assuming this error message exists
            );
        }

        projectEmployeeRepository.deleteById(id);
    }

    @Override
    public ProjectWithEmployeesDto getProjectWithEmployees(String projectCode) {
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        List<ProjectEmployeeDto> employees = getEmployeesByProject(projectCode);

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
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        projectRepository.delete(project);
    }

    @Override
    public String updateEmployeeStatus(String projectCode, String employeeCode, boolean newStatus) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        ProjectEmployee projectEmployee = projectEmployeeRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));

        projectEmployee.setActive(newStatus);  // <-- set the new status
        projectEmployeeRepository.save(projectEmployee);

        return String.format(MessageConstants.PROJECT_STATUS_UPDATED, employeeCode, projectCode);
    }

    @Override
    public String updateEmployee(String projectCode, String employeeCode, AssignEmployeesDto.EmployeeAssignment dto) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        ProjectEmployee projectEmployee = projectEmployeeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));
        validateEmployeeDates(dto, project.getStartDate(), project.getEndDate(), projectCode);
        projectEmployee.setStartDate(dto.getStartDate());
        projectEmployee.setEndDate(dto.getEndDate());
        projectEmployee.setRole_in_project(dto.getRole_in_project());
        projectEmployeeRepository.save(projectEmployee);
        return String.format(MessageConstants.PROJECT_STATUS_UPDATED, employeeCode, projectCode);
    }

    @Override
    public List<ProjectDto> getProjectsByEmployeeCode(String employeeCode) {
        List<ProjectEmployee> assignments = projectEmployeeRepository.findByIdEmployeeCodeIgnoreCaseAndIsActiveTrue(employeeCode);

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
    @Override
    public List<Map<String, String>> getUnassignedUsersForProject(String projectCode) {
        Project project = projectRepository.findByProjectCodeAndIsActiveTrue(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode)
                ));

        Set<String> assignedEmployeeCodes = projectEmployeeRepository.findByProject_ProjectCode(projectCode).stream()
                .map(pe -> pe.getId().getEmployeeCode())
                .collect(Collectors.toSet());

        List<Map<String, String>> allUsers = identityServiceClient.getAllUsers().getBody();

        return allUsers.stream()
                .filter(user -> !assignedEmployeeCodes.contains(user.get("employeeCode")))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectWithEmployeesDto> getProjectsWithEmployeesUnderManager(String projectManagerCode) {
        List<Project> projects = projectRepository.findByProjectManagerCodeIgnoreCaseAndIsActiveTrue(projectManagerCode);

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
                        .isActive(pe.isActive())
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
                .isActive(project.isActive())
                .build();
    }


    private void validateEmployeeDates(AssignEmployeesDto.EmployeeAssignment emp,
                                       Timestamp projectStart,
                                       Timestamp projectEnd,
                                       String projectCode) {

        Timestamp empStart = emp.getStartDate();
        Timestamp empEnd   = emp.getEndDate(); // may be null

        if (empStart == null) {
            throw new TimeSheetException(errorCode.VALIDATION_ERROR,
                    String.format(errorMessage.START_DATE_REQUIRED, emp.getEmployeeCode()));
        }

        if (empStart.before(projectStart)) {
            throw new TimeSheetException(errorCode.VALIDATION_ERROR,
                    String.format(errorMessage.EMP_START_BEFORE_PROJECT,
                            emp.getEmployeeCode(), projectCode));
        }

        if (projectEnd != null && empEnd != null && empEnd.after(projectEnd)) {
            throw new TimeSheetException(errorCode.VALIDATION_ERROR,
                    String.format(errorMessage.EMP_END_AFTER_PROJECT,
                            emp.getEmployeeCode(), projectCode));
        }

        if (empEnd != null && empEnd.before(empStart)) {
            throw new TimeSheetException(errorCode.VALIDATION_ERROR,
                    String.format(errorMessage.END_BEFORE_START, emp.getEmployeeCode()));
        }
    }

}