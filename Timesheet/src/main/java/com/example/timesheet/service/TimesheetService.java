package com.example.timesheet.service;

import com.example.timesheet.Repository.TimesheetEntryRepository;
import com.example.timesheet.client.IdentityServiceClient;
import com.example.timesheet.dto.request.UserIdentityDto;
import com.example.timesheet.enums.EntryType;
import com.example.timesheet.models.TimesheetEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetEntryRepository timesheetRepo;
    private final ProjectAssignmentService assignmentService;
    private final IdentityServiceClient identityServiceClient;

    public TimesheetEntry createEntry(TimesheetEntry entry) {
        if (entry.getEntryType() == EntryType.PROJECT) {
            List<Long> assignedProjects = assignmentService.getAssignedProjectIds(entry.getUserId());

            if (!assignedProjects.contains(entry.getProjectId())) {
                throw new IllegalArgumentException("User not assigned to project");
            }
        }

        // Get user info to populate username from IdentityService
        UserIdentityDto user = identityServiceClient.getUserById(entry.getUserId()).getBody();
        entry.setUsername(user.getUsername());

        return timesheetRepo.save(entry);
    }

    public List<EntryType> getEntryTypes() {
        return List.of(EntryType.values());
    }
}
