package com.timesheet.audit.listener;


import com.timesheet.audit.service.AuditStoreService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.example.common.audit.AuditEvent;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditListener {

    private final AuditStoreService auditStoreService;

    @PostConstruct
    public void init() {
        log.info("✅ AuditListener is active and waiting for messages...");
    }


    @KafkaListener(
            topics = "audit-trail-topic",
            groupId = "audit-group",
            containerFactory = "auditKafkaListenerFactory"
    )    public void listenToAuditEvents(AuditEvent event) {
        log.info(" Received Audit Event: {}", event);
        auditStoreService.saveAudit(event);
    }
}

