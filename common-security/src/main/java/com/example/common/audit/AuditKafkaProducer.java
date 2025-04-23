package com.example.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditKafkaProducer {

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

    @Value("${audit.topic.name:audit-trail-topic}")
    private String auditTopic;

    public void sendAudit(AuditEvent event) {
        kafkaTemplate.send(auditTopic, event);
    }
}
