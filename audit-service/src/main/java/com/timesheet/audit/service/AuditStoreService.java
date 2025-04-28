package com.timesheet.audit.service;


import com.example.common.audit.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timesheet.audit.model.AuditRecord;
import com.timesheet.audit.Repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditStoreService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;  // Inject ObjectMapper for serialization

    public void saveAudit(AuditEvent event) {
        try {
            // Explicitly serialize the details to JSON
            String detailsJson = objectMapper.writeValueAsString(event.getDetails());

            AuditRecord record = AuditRecord.builder()
                    .serviceName(event.getServiceName())
                    .actor(event.getActor())
                    .action(event.getAction())
                    .details(detailsJson)  // Store as JSON string in the details field
                    .timestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                    .build();

            auditRepository.save(record);
        } catch (Exception e) {
            e.printStackTrace();  // Log exception if needed
        }
    }
}