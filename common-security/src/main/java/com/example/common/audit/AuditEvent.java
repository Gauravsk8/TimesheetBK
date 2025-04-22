package com.example.common.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuditEvent {
    private String serviceName;
    private String actor;
    private String action;
    private Instant timestamp;
    private Map<String, Object> details;
}