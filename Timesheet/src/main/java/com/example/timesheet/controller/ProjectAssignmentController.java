package com.example.timesheet.controller;

import com.example.timesheet.service.ProjectAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Project")
@RequiredArgsConstructor
public class ProjectAssignmentController {

    private final ProjectAssignmentService service;

    @PostMapping("/assign")
    public ResponseEntity<?> assignProject(@RequestParam String userId,
                                           @RequestParam Long projectId) {
        return ResponseEntity.ok(service.assignProjectToUser(userId, projectId));
    }

    @GetMapping("/assigned-projects")
    public ResponseEntity<List<Long>> getAssignedProjects(@RequestParam String userId) {
        return ResponseEntity.ok(service.getAssignedProjectIds(userId));
    }
}

