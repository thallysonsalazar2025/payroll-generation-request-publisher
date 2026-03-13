package com.example.payrollpublisher.service;

import com.example.payrollpublisher.controller.PayrollGenerationRequestPublisher;
import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.logging.RequestLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayrollRequestService {
    private static final Logger log = LoggerFactory.getLogger(PayrollRequestService.class);

    private final PayrollGenerationRequestPublisher payrollGenerationRequestPublisher;

    public PayrollRequestService(PayrollGenerationRequestPublisher payrollGenerationRequestPublisher) {
        this.payrollGenerationRequestPublisher = payrollGenerationRequestPublisher;
    }

    public String publish(PayrollGenerationRequest request) {
        log.trace("Starting publish workflow");
        log.debug("Publishing sanitized request: {}", RequestLogFormatter.summarize(request));

        request.validateBusinessRules();

        String requestId = UUID.randomUUID().toString();
        log.info("Generated requestId={} for payroll publication", requestId);
        
        payrollGenerationRequestPublisher.publish(request);

        return requestId;
    }
}
