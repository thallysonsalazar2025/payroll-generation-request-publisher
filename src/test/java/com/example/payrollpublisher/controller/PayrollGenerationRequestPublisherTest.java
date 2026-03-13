package com.example.payrollpublisher.controller;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.dto.PayrollMessage;
import com.example.payrollpublisher.mappers.PayrollMapper;
import com.example.payrollpublisher.serialization.PayrollMessageSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static com.example.payrollpublisher.config.RabbitMqConfig.EXG_NAME_PAYROLL_GENERATION;
import static com.example.payrollpublisher.config.RabbitMqConfig.ROUTING_KEY_PAY_GEN;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollGenerationRequestPublisherTest {

    @Mock
    AmqpTemplate amqpTemplate;

    @Mock
    private PayrollMapper payrollMapper;

    @Mock
    private PayrollMessageSerializer payrollMessageSerializer;

    @InjectMocks
    private PayrollGenerationRequestPublisher publisher;

    private PayrollGenerationRequest request;
    private PayrollMessage message;
    private String serializedMessage;

    @BeforeEach
    void setUp() {
        request = new PayrollGenerationRequest("emp123", "comp456", "req789", 10, 2023);
        message = new PayrollMessage("emp123", "comp456", "req789", 10, 2023);
        serializedMessage = "{\"employeeId\":\"emp123\",\"companyId\":\"comp456\",\"requesterId\":\"req789\",\"month\":10,\"year\":2023}";
    }

    @Test
    @DisplayName("Should publish message successfully when request is valid")
    void publish_whenRequestIsValid_shouldSendMessageToBroker() {
        // Arrange
        when(payrollMapper.toMessage(request)).thenReturn(message);
        when(payrollMessageSerializer.serialize(message)).thenReturn(serializedMessage);

        // Act
        publisher.publish(request);

        // Assert
        verify(payrollMapper).toMessage(request);
        verify(payrollMessageSerializer).serialize(message);
        verify(amqpTemplate).convertAndSend(
                EXG_NAME_PAYROLL_GENERATION,
                ROUTING_KEY_PAY_GEN,
                serializedMessage
        );
        verifyNoMoreInteractions(amqpTemplate, payrollMapper, payrollMessageSerializer);
    }

    @Test
    @DisplayName("Should throw exception and not send message when serialization fails")
    void publish_whenSerializationFails_shouldThrowExceptionAndNotSend() {
        // Arrange
        when(payrollMapper.toMessage(request)).thenReturn(message);
        RuntimeException serializationException = new RuntimeException("Serialization failed");
        when(payrollMessageSerializer.serialize(message)).thenThrow(serializationException);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> publisher.publish(request));

        verify(payrollMapper).toMessage(request);
        verify(payrollMessageSerializer).serialize(message);
        verifyNoInteractions(amqpTemplate);
    }
}
