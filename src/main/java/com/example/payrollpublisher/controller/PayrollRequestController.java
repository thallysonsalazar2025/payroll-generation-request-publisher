package com.example.payrollpublisher.controller;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.service.PayrollRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payroll-generation-requests")
public class PayrollRequestController {

    private final PayrollRequestService service;

    public PayrollRequestController(PayrollRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody PayrollGenerationRequest request) {
        String requestId = service.publish(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "status", "QUEUED",
                        "requestId", requestId
                ));
    }
}
