package com.timesheet.audit.service;


import com.example.common.audit.AuditEvent;
import com.timesheet.audit.model.AuditRecord;
import com.timesheet.audit.Repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditStoreService {

    private final AuditRepository auditRepository;

    public void saveAudit(AuditEvent event) {
        AuditRecord record = AuditRecord.builder()
                .serviceName(event.getServiceName())
                .actor(event.getActor())
                .action(event.getAction())
                .details(event.getDetails() != null ? event.getDetails().toString() : null)
                .timestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                .build();

        auditRepository.save(record);
    }
}
