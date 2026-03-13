package com.example.payrollpublisher.dto;

public record PayrollMessage(
        String employeeId,
        String companyId,
        String requesterId,
        Integer month,
        Integer year
) {
}
