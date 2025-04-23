package com.example.timesheet.service;


import com.example.timesheet.Repository.UserProjectAssignmentRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.models.UserProjectAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAssignmentService {

    private final UserProjectAssignmentRepository assignmentRepo;
    private final IdentityServiceClient identityServiceClient;

    public UserProjectAssignment assignProjectToUser(String userId, Long projectId) {
        if (assignmentRepo.existsByUserIdAndProjectId(userId, projectId)) {
            throw new IllegalArgumentException("Project already assigned to user");
        }

        // Optional: Validate user from IdentityService before assigning
        identityServiceClient.getUserById(userId);

        return assignmentRepo.save(UserProjectAssignment.builder()
                .userId(userId)
                .projectId(projectId)
                .build());
    }

    public List<UserIdentityDto> getUserProjects(String userId) {
        List<UserProjectAssignment> assignments = assignmentRepo.findByUserId(userId);

        UserIdentityDto user = identityServiceClient.getUserById(userId).getBody();
        return assignments.stream()
                .map(a -> new UserIdentityDto(user.getKeycloakUserId(), user.getUsername()))
                .toList();
    }

    public List<Long> getAssignedProjectIds(String userId) {
        return assignmentRepo.findByUserId(userId)
                .stream().map(UserProjectAssignment::getProjectId)
                .toList();
    }
}
