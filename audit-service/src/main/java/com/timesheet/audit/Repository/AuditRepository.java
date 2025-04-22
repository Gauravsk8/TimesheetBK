package com.timesheet.audit.Repository;


import com.timesheet.audit.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}

