package com.example.timesheet.Repository;

import com.example.timesheet.models.UserProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProjectAssignmentRepository extends JpaRepository<UserProjectAssignment, Long> {
    List<UserProjectAssignment> findByUserId(String userId);
    boolean existsByUserIdAndProjectId(String userId, Long projectId);
}
