package com.example.payrollpublisher.integration;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.dto.PayrollMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;

import static com.example.payrollpublisher.config.RabbitMqConfig.EXG_NAME_PAYROLL_GENERATION;
import static com.example.payrollpublisher.config.RabbitMqConfig.ROUTING_KEY_PAY_GEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PayrollIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "amqpTemplate")
    private AmqpTemplate amqpTemplate;

    @Test
    @DisplayName("Integration Test: Should process valid payroll request and publish message")
    void shouldProcessPayrollRequestSuccessfully() throws Exception {
        PayrollGenerationRequest request = new PayrollGenerationRequest(
                "emp-123",
                "comp-456",
                "user-789",
                11,
                Year.now().getValue()
        );

        mockMvc.perform(post("/api/v1/payslip-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.requestId").exists());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(amqpTemplate).convertAndSend(
                eq(EXG_NAME_PAYROLL_GENERATION),
                eq(ROUTING_KEY_PAY_GEN),
                messageCaptor.capture()
        );

        PayrollMessage published = objectMapper.readValue(messageCaptor.getValue(), PayrollMessage.class);
        assertThat(published.employeeId()).isEqualTo(request.employeeId());
        assertThat(published.companyId()).isEqualTo(request.companyId());
        assertThat(published.requesterId()).isEqualTo(request.requesterId());
        assertThat(published.month()).isEqualTo(request.month());
        assertThat(published.year()).isEqualTo(request.year());
    }

    @Test
    @DisplayName("Integration Test: Should reject invalid payroll request (future year) and not publish")
    void shouldRejectInvalidPayrollRequest() throws Exception {
        int futureYear = Year.now().getValue() + 5;
        PayrollGenerationRequest request = new PayrollGenerationRequest(
                "emp-123",
                "comp-456",
                "user-789",
                1,
                futureYear
        );

        mockMvc.perform(post("/api/v1/payslip-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid business rule"))
                .andExpect(jsonPath("$.details[0]").value("year cannot be greater than " + (Year.now().getValue() + 1)));

        verify(amqpTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
