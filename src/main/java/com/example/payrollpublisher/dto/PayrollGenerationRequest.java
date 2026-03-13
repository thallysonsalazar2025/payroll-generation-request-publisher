package com.example.payrollpublisher.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Year;

public record PayrollGenerationRequest(
        @NotBlank(message = "employeeId is required")
        String employeeId,

        @NotBlank(message = "companyId is required")
        String companyId,

        @NotBlank(message = "requesterId is required")
        String requesterId,

        @NotNull(message = "month is required")
        @Min(value = 1, message = "month must be between 1 and 12")
        @Max(value = 12, message = "month must be between 1 and 12")
        Integer month,

        @NotNull(message = "year is required")
        @Min(value = 2000, message = "year must be greater than or equal to 2000")
        Integer year
) {
    public void validateBusinessRules() {
        int maxYear = Year.now().getValue() + 1;
        if (year != null && year > maxYear) {
            throw new IllegalArgumentException("year cannot be greater than " + maxYear);
        }
    }
}
