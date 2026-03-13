package com.example.payrollpublisher.controller;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.logging.RequestLogFormatter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollRequestController {

    private static final Logger log = LoggerFactory.getLogger(PayrollRequestController.class);

    private final PayrollGenerationRequestPublisher publisher;

    public PayrollRequestController(PayrollGenerationRequestPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/generation-request")
    public ResponseEntity<Void> createRequest(@Valid @RequestBody PayrollGenerationRequest request) {
        // Usando o seu formatador de log seguro aqui na entrada da API
        log.info("Received API request: {}", RequestLogFormatter.summarize(request));
        publisher.publish(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}