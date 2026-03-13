package com.example.payrollpublisher.controller;

import com.example.payrollpublisher.service.PayrollRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollRequestController.class)
class PayrollRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollRequestService service;

    @Test
    void shouldReturnAcceptedWhenRequestIsValid() throws Exception {
        when(service.publish(any())).thenReturn("request-123");

        mockMvc.perform(post("/api/payroll-generation-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-1",
                                  "companyId": "comp-1",
                                  "requesterId": "req-1",
                                  "month": 5,
                                  "year": 2025
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.requestId").value("request-123"));
    }

    @Test
    void shouldReturnStandardizedValidationErrorWhenMonthIsInvalid() throws Exception {
        mockMvc.perform(post("/api/payroll-generation-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-1",
                                  "companyId": "comp-1",
                                  "requesterId": "req-1",
                                  "month": 13,
                                  "year": 2025
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"))
                .andExpect(jsonPath("$.details[0]").value("month: month deve ser entre 1 e 12"))
                .andExpect(jsonPath("$.path").value("/api/payroll-generation-requests"));
    }


    @Test
    void shouldReturnStandardizedValidationErrorWhenPayloadIsMalformed() throws Exception {
        mockMvc.perform(post("/api/payroll-generation-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-1",
                                  "companyId": "comp-1",
                                  "requesterId": "req-1",
                                  "month": "abc",
                                  "year": 2025
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"))
                .andExpect(jsonPath("$.details[0]").value("Payload de requisição malformado"))
                .andExpect(jsonPath("$.path").value("/api/payroll-generation-requests"));
    }

    @Test
    void shouldReturnStandardizedBusinessErrorWhenYearIsTooHigh() throws Exception {
        int invalidYear = Year.now().getValue() + 2;
        when(service.publish(any())).thenThrow(new IllegalArgumentException("year não pode ser maior que " + (invalidYear - 1)));

        mockMvc.perform(post("/api/payroll-generation-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-1",
                                  "companyId": "comp-1",
                                  "requesterId": "req-1",
                                  "month": 12,
                                  "year": %d
                                }
                                """.formatted(invalidYear)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Regra de negócio inválida"))
                .andExpect(jsonPath("$.details[0]").exists())
                .andExpect(jsonPath("$.path").value("/api/payroll-generation-requests"));
    }
}
