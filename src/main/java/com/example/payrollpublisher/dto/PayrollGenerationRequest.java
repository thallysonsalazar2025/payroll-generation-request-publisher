package com.example.payrollpublisher.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayrollGenerationRequest(
        @NotBlank(message = "employeeId é obrigatório")
        String employeeId,

        @NotBlank(message = "companyId é obrigatório")
        String companyId,

        @NotBlank(message = "requesterId é obrigatório")
        String requesterId,

        @NotNull(message = "month é obrigatório")
        @Min(value = 1, message = "month deve ser entre 1 e 12")
        @Max(value = 12, message = "month deve ser entre 1 e 12")
        Integer month,

        @NotNull(message = "year é obrigatório")
        @Min(value = 2000, message = "year deve ser maior ou igual a 2000")
        Integer year
) {
}
