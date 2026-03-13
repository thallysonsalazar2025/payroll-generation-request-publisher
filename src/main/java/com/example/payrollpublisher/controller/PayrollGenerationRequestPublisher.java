package com.example.payrollpublisher.controller;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.dto.PayrollMessage;
import com.example.payrollpublisher.mappers.PayrollMapper;
import com.example.payrollpublisher.serialization.PayrollMessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import static com.example.payrollpublisher.config.RabbitMqConfig.EXG_NAME_PAYROLL_GENERATION;
import static com.example.payrollpublisher.config.RabbitMqConfig.ROUTING_KEY_PAY_GEN;

@Component
public class PayrollGenerationRequestPublisher {
    private static final Logger log = LoggerFactory.getLogger(PayrollGenerationRequestPublisher.class);
    
    private final AmqpTemplate amqpTemplate;
    private final PayrollMapper payrollMapper;
    private final PayrollMessageSerializer payrollMessageSerializer;

    public PayrollGenerationRequestPublisher(AmqpTemplate amqpTemplate, 
                                             PayrollMapper payrollMapper, 
                                             PayrollMessageSerializer payrollMessageSerializer) {
        this.amqpTemplate = amqpTemplate;
        this.payrollMapper = payrollMapper;
        this.payrollMessageSerializer = payrollMessageSerializer;
    }

    public void publish(PayrollGenerationRequest request) {
        log.info("Preparing to publish payroll generation request for employee: {}", request.employeeId());
        
        PayrollMessage messageDto = payrollMapper.toMessage(request);
        String messageBody = payrollMessageSerializer.serialize(messageDto);
        
        log.debug("Dispatching payroll generation message (length={} chars)", messageBody.length());
        amqpTemplate.convertAndSend(EXG_NAME_PAYROLL_GENERATION, ROUTING_KEY_PAY_GEN, messageBody);
        log.info("Message sent to broker");
    }
}
