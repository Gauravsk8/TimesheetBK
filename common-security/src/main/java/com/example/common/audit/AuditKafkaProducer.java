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

    public String sendAudit(AuditEvent event) {
        System.out.println("hasan and gaurav is loging "+event.toString());
        kafkaTemplate.send(auditTopic, event);
        return "hasan and gaurav is logging";
    }
}
