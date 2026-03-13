package com.example.payrollpublisher.serialization;

import com.example.payrollpublisher.dto.PayrollMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayrollMessageSerializer {
    
    private static final Logger log = LoggerFactory.getLogger(PayrollMessageSerializer.class);
    private final ObjectMapper objectMapper;

    public PayrollMessageSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(PayrollMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payroll message for employee: {}", message.employeeId(), e);
            throw new RuntimeException("Error serializing payroll message", e);
        }
    }
}
