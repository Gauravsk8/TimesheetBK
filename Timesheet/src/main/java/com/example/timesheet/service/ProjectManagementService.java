package com.example.timesheet.service;


import com.example.common.exceptions.TimeSheetException;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.timesheet.Repository.CostCenterRepository;
import com.example.timesheet.Repository.ProjectEmployeeRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.dto.request.*;
import com.example.timesheet.enums.EmployeeStatus;
import com.example.timesheet.keys.ProjectEmployeeId;
import com.example.timesheet.models.Clients;
import com.example.timesheet.models.CostCenter;
import com.example.timesheet.models.Project;
import com.example.timesheet.models.ProjectEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.timesheet.Repository.ClientsRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectManagementService {

    private final ClientsRepository clientsRepository;
    private final CostCenterRepository costCenterRepository;
    private final IdentityServiceClient identityServiceClient;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;



    public String createClient(ClientDto dto) {
        Clients client = new Clients();
        client.setName(dto.getName());
        client.setContactPerson(dto.getContactPerson());
        client.setContactEmail(dto.getContactEmail());
        client.setAddress(dto.getAddress());
        Clients savedClient = clientsRepository.save(client);
        return String.format("Client created %s",savedClient.getId());
    }

    public List<ClientResponseDto> getAllClients() {
        return clientsRepository.findAll()
                .stream()
                .map(client -> new ClientResponseDto(
                        client.getId(),
                        client.getName(),
                        client.getContactPerson(),
                        client.getContactEmail(),
                        client.getAddress()
                ))
                .toList();
    }



    public String updateClient(Long id, ClientDto dto) {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR,String.format(errorMessage.CLIENT_NOT_FOUND,id)));

        client.setName(dto.getName());
        client.setContactPerson(dto.getContactPerson());
        client.setContactEmail(dto.getContactEmail());
        client.setAddress(dto.getAddress());
        Clients savedClient = clientsRepository.save(client);
        return String.format("Client updated %s",savedClient.getId());
    }

    public Optional<ClientResponseDto> getClientById(Long id) {
        Optional<Clients> clientOptional = clientsRepository.findById(id);

        return clientOptional.map(client -> new ClientResponseDto(
                client.getId(),
                client.getName(),
                client.getContactEmail(),
                client.getContactPerson(),
                client.getAddress()
        ));
    }


    public String createCostCenter(CostCenterDto dto) {
        CostCenter costCenter = CostCenter.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .managerCode(dto.getManagerCode())
                .build();

        CostCenter saveCostCenter=costCenterRepository.save(costCenter);
        return String.format("Cost Center created %s",saveCostCenter.getCode());
    }

    public List<CostCenterResponseDto> getAllCostCenters() {
        return costCenterRepository.findAll().stream()
                .map(costCenter -> new CostCenterResponseDto(
                        costCenter.getCode(),
                        costCenter.getName(),
                        costCenter.getDescription(),
                        costCenter.getManagerCode()
                ))
                .collect(Collectors.toList());
    }

    public CostCenterResponseDto getCostCenterByCode(String code) {
        CostCenter costCenter = costCenterRepository.findById(code)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.COST_CENTER_NOT_FOUND, code)
                ));

        // Map the CostCenter entity to CostCenterResponseDto and return
        return new CostCenterResponseDto(
                costCenter.getCode(),
                costCenter.getName(),
                costCenter.getDescription(),
                costCenter.getManagerCode()
        );
    }


    public String updateCostCenter(String code, CostCenterDto dto) {
        CostCenter costCenter = costCenterRepository.findById(code)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.COST_CENTER_NOT_FOUND, code)
                ));
        costCenter.setName(dto.getName());
        costCenter.setDescription(dto.getDescription());
        costCenter.setManagerCode(dto.getManagerCode());
        CostCenter saveCostCenter=costCenterRepository.save(costCenter);
        return String.format("Cost Center created %s",saveCostCenter.getCode());

    }

    public String createProject(ProjectDto dto) {

        if (projectRepository.existsById(dto.getProjectCode())) {
            throw new TimeSheetException(
                    errorCode.PROJECT_ALREADY_EXISTS, // Assuming this is the error code
                    String.format(errorMessage.PROJECT_ALREADY_EXISTS, dto.getProjectCode()) // Assuming you have this error message in your errorMessage class
            );
        }

        Clients client = clientsRepository.findById(dto.getClientId())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Error code for client not found
                        String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId()) // Assuming error message for client not found
                ));

        CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode())
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Error code for cost center not found
                        String.format(errorMessage.COST_CENTER_NOT_FOUND, dto.getCostCenterCode()) // Error message for cost center not found
                ));
        ResponseEntity<Boolean> managerRole=identityServiceClient.hasProjectManagerRole(dto.getManagerCode());

        if(!managerRole.getBody())
        {
            throw new TimeSheetException(
                    errorCode.MANAGER_ROLE, // Assuming this is the error code
                    String.format(errorMessage.PROJECT_MANAGER_ROLE, dto.getManagerCode()) // Assuming you have this error message in your errorMessage class
            );
        }

        Project project = new Project();
        project.setProjectCode(dto.getProjectCode());
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setOwner(dto.getOwner());
        project.setStart_date(dto.getStartDate());
        project.setEnd_date(dto.getEndDate());
        project.setClients(client);
        project.setCostCenter(costCenter);
        project.setManagerCode(dto.getManagerCode());
        project.setAllocated_hours(dto.getAllocatedHours());

        Project savedProject=projectRepository.save(project);

        return "Project " + savedProject.getTitle() + " has been created";
    }

    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Project getProjectByCode(String code) {
        return projectRepository.findById(code)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, code) // Assuming you have this error message in your errorMessage class
                ));
    }

    public String updateProject(String code, ProjectDto dto) {
        Project project = getProjectByCode(code);

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setOwner(dto.getOwner());
        project.setStart_date(dto.getStartDate());
        project.setEnd_date(dto.getEndDate());
        project.setManagerCode(dto.getManagerCode());
        project.setAllocated_hours(dto.getAllocatedHours());

        if (!project.getClients().getId().equals(dto.getClientId())) {
            Clients client = clientsRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR, // Error code for client not found
                            String.format(errorMessage.CLIENT_NOT_FOUND, dto.getClientId()) // Assuming error message for client not found
                    ));
            project.setClients(client);
        }

        if (!project.getCostCenter().getCode().equals(dto.getCostCenterCode())) {
            CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode())
                    .orElseThrow(() -> new TimeSheetException(
                            errorCode.NOT_FOUND_ERROR, // Error code for cost center not found
                            String.format(errorMessage.COST_CENTER_NOT_FOUND, dto.getCostCenterCode()) // Error message for cost center not found
                    ));
            project.setCostCenter(costCenter);
        }

        Project updatedProject=projectRepository.save(project);

        return "Project " + updatedProject.getTitle() + " has been updated";
    }

    public String assignEmployeesToProject(AssignEmployeesDto dto, String projectCode) {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        List<ProjectEmployee> assignments = dto.getEmployees().stream()
                .filter(emp -> {
                    ProjectEmployeeId id = new ProjectEmployeeId(projectCode, emp.getEmployeeCode());
                    return !projectEmployeeRepository.existsById(id); // avoid duplicates
                })
                .map(emp -> {
                    ProjectEmployee pe = new ProjectEmployee();
                    ResponseEntity<UserIdentityDto> user= identityServiceClient.getUserByemployeeCode(emp.getEmployeeCode());
                    String EmployeeKeycloakId = user.getBody().getKeycloakUserId();
                    pe.setId(new ProjectEmployeeId(projectCode, emp.getEmployeeCode()));
                    pe.setProject(project);
                    pe.setEmployeeKeycloakId(EmployeeKeycloakId);
                    pe.setStartDate(emp.getStartDate());
                    pe.setEndDate(emp.getEndDate());
                    pe.setStatus(EmployeeStatus.ACTIVATE);
                    return pe;
                }).toList();

        projectEmployeeRepository.saveAll(assignments);
        return assignments.isEmpty()
                ? "No new employees assigned (all were already assigned)"
                : assignments.size() + " employee(s) assigned successfully.";
    }

    public List<ProjectEmployeeDto> getEmployeesByProject(String projectCode) {
        List<ProjectEmployee> entities = projectEmployeeRepository.findByProject_ProjectCodeIgnoreCase(projectCode);

        return entities.stream().map(pe -> {
            ProjectEmployeeDto dto = new ProjectEmployeeDto();
            ResponseEntity<UserIdentityDto> user= identityServiceClient.getUserByemployeeCode(pe.getId().getEmployeeCode());
            dto.setEmployeeCode(pe.getId().getEmployeeCode());
            dto.setFirstName(user.getBody().getFirstName());
            dto.setLastName(user.getBody().getLastName());
            dto.setStartDate(pe.getStartDate());
            dto.setEndDate(pe.getEndDate());
            dto.setStatus(pe.getStatus());
            return dto;
        }).toList();
    }

    public void removeEmployeeFromProject(String projectCode, String employeeCode) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        if (!projectEmployeeRepository.existsById(id)) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,  // Assuming this is the error code
                    String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)  // Assuming this error message exists
            );        }

        projectEmployeeRepository.deleteById(id);
    }

    public ProjectWithEmployeesDto getProjectWithEmployees(String projectCode) {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        List<ProjectEmployeeDto> employees = getEmployeesByProject(projectCode);

        return ProjectWithEmployeesDto.builder()
                .projectCode(project.getProjectCode())
                .title(project.getTitle())
                .description(project.getDescription())
                .owner(project.getOwner())
                .startDate(project.getStart_date())
                .endDate(project.getEnd_date())
                .costCenterCode(project.getCostCenter().getCode())
                .clientName(project.getClients().getName())
                .managerCode(project.getManagerCode())
                .employees(employees)
                .build();

    }

    public void deleteProject(String projectCode) {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR, // Assuming this is the error code
                        String.format(errorMessage.PROJECT_NOT_FOUND, projectCode) // Assuming you have this error message in your errorMessage class
                ));

        projectRepository.delete(project);
    }

    public String updateEmployeeStatus(String projectCode, String employeeCode, String newStatus) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        ProjectEmployee projectEmployee = projectEmployeeRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));

        EmployeeStatus statusEnum;
        try {
            statusEnum = EmployeeStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TimeSheetException(errorCode.NOT_FOUND_ERROR, "Status Not Found: " + newStatus);
        }

        projectEmployee.setStatus(statusEnum);  // <-- set the new status
        projectEmployeeRepository.save(projectEmployee);

        return String.format("Updated Employee status of %s in project %s", employeeCode, projectCode);
    }


    public String updateEmployee(String projectCode, String employeeCode, AssignEmployeesDto.EmployeeAssignment dto) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        ProjectEmployee projectEmployee = projectEmployeeRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.ASSIGNMENT_NOT_FOUND, projectCode, employeeCode)
                ));
        projectEmployee.setStartDate(dto.getStartDate());
        projectEmployee.setEndDate(dto.getEndDate());
        projectEmployeeRepository.save(projectEmployee);
        return String.format("Updated Employee %s in project %s",employeeCode,projectCode);
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
                    dto.setOwner(project.getOwner());
                    dto.setStartDate(project.getStart_date());
                    dto.setEndDate(project.getEnd_date());
                    dto.setManagerCode(project.getManagerCode());
                    dto.setAllocatedHours(project.getAllocated_hours());

                    return dto;
                })
                .toList();
    }



    private ProjectResponseDto mapToResponse(Project project) {
        return ProjectResponseDto.builder()
                .projectCode(project.getProjectCode())
                .title(project.getTitle())
                .description(project.getDescription())
                .owner(project.getOwner())
                .startDate(project.getStart_date())
                .endDate(project.getEnd_date())
                .clientName(project.getClients().getName())
                .costCenterCode(project.getCostCenter().getCode())
                .managerCode(project.getManagerCode())
                .allocatedHours(project.getAllocated_hours())
                .build();
    }

}
