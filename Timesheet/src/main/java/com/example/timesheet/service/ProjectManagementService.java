package com.example.timesheet.service;


import com.example.timesheet.Repository.CostCenterRepository;
import com.example.timesheet.Repository.ProjectEmployeeRepository;
import com.example.timesheet.Repository.ProjectRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.reponse.ProjectResponseDto;
import com.example.timesheet.dto.reponse.ProjectWithEmployeesDto;
import com.example.timesheet.dto.request.*;
import com.example.timesheet.dto.reponse.ProjectEmployeeDto;
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

    public List<Clients> getAllClients() {
        return clientsRepository.findAll();
    }

    public Clients updateClient(Long id, ClientDto dto) {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        client.setName(dto.getName());
        client.setContactPerson(dto.getContactPerson());
        client.setContactEmail(dto.getContactEmail());
        client.setAddress(dto.getAddress());

        return clientsRepository.save(client);
    }

    public Optional<Clients> getClientById(Long id) {
        return clientsRepository.findById(id);
    }

    public CostCenter createCostCenter(CostCenterDto dto) {
        CostCenter costCenter = CostCenter.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .managerCode(dto.getManagerCode())
                .build();

        return costCenterRepository.save(costCenter);
    }

    public List<CostCenter> getAllCostCenters() {
        return costCenterRepository.findAll();
    }

    public CostCenter getCostCenterByCode(String code) {
        return costCenterRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("Cost center not found with code: " + code));
    }

    public CostCenter updateCostCenter(String code, CostCenterDto dto) {
        CostCenter costCenter = getCostCenterByCode(code);

        costCenter.setName(dto.getName());
        costCenter.setDescription(dto.getDescription());
        costCenter.setManagerCode(dto.getManagerCode());

        return costCenterRepository.save(costCenter);
    }

    public String createProject(ProjectDto dto) {

        if (projectRepository.existsById(dto.getProjectCode())) {
            throw new RuntimeException("Project with code '" + dto.getProjectCode() + "' already exists");
        }

        Clients client = clientsRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode())
                .orElseThrow(() -> new RuntimeException("Cost Center not found"));

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
                .orElseThrow(() -> new RuntimeException("Project not found"));
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
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            project.setClients(client);
        }

        if (!project.getCostCenter().getCode().equals(dto.getCostCenterCode())) {
            CostCenter costCenter = costCenterRepository.findById(dto.getCostCenterCode())
                    .orElseThrow(() -> new RuntimeException("Cost Center not found"));
            project.setCostCenter(costCenter);
        }

        Project updatedProject=projectRepository.save(project);

        return "Project " + updatedProject.getTitle() + " has been updated";
    }

    public String assignEmployeesToProject(AssignEmployeesDto dto) {
        Project project = projectRepository.findById(dto.getProjectCode())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<ProjectEmployee> assignments = dto.getEmployees().stream()
                .filter(emp -> {
                    ProjectEmployeeId id = new ProjectEmployeeId(dto.getProjectCode(), emp.getEmployeeCode());
                    return !projectEmployeeRepository.existsById(id); // avoid duplicates
                })
                .map(emp -> {
                    ProjectEmployee pe = new ProjectEmployee();
                    ResponseEntity<UserIdentityDto> user= identityServiceClient.getUserByemployeeCode(emp.getEmployeeCode());
                    String EmployeeKeycloakId = user.getBody().getKeycloakUserId();
                    pe.setId(new ProjectEmployeeId(dto.getProjectCode(), emp.getEmployeeCode()));
                    pe.setProject(project);
                    pe.setEmployeeKeycloakId(EmployeeKeycloakId);
                    return pe;
                }).toList();

        projectEmployeeRepository.saveAll(assignments);
        return assignments.isEmpty()
                ? "No new employees assigned (all were already assigned)"
                : assignments.size() + " employee(s) assigned successfully.";
    }

    public List<ProjectEmployeeDto> getEmployeesByProject(String projectCode) {
        List<ProjectEmployee> entities = projectEmployeeRepository.findByProject_ProjectCode(projectCode);

        return entities.stream().map(pe -> {
            ProjectEmployeeDto dto = new ProjectEmployeeDto();
            ResponseEntity<UserIdentityDto> user= identityServiceClient.getUserByemployeeCode(pe.getId().getEmployeeCode());
            dto.setEmployeeCode(pe.getId().getEmployeeCode());
            dto.setFirstName(user.getBody().getFirstName());
            dto.setLastName(user.getBody().getLastName());
            return dto;
        }).toList();
    }

    public void removeEmployeeFromProject(String projectCode, String employeeCode) {
        ProjectEmployeeId id = new ProjectEmployeeId(projectCode, employeeCode);

        if (!projectEmployeeRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found for given project and employee");
        }

        projectEmployeeRepository.deleteById(id);
    }

    public ProjectWithEmployeesDto getProjectWithEmployees(String projectCode) {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new RuntimeException("Project not found"));

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
                .employees(employees)
                .build();

    }

    public void deleteProject(String projectCode) {
        Project project = projectRepository.findById(projectCode)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
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
