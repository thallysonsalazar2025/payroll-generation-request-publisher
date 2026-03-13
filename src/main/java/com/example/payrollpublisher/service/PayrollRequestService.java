package com.example.payrollpublisher.service;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

@Service
public class PayrollRequestService {

    public String publish(PayrollGenerationRequest request) {
        int maxYear = Year.now().getValue() + 1;
        if (request.year() > maxYear) {
            throw new IllegalArgumentException("year não pode ser maior que " + maxYear);
        }

        return UUID.randomUUID().toString();
    }
}
