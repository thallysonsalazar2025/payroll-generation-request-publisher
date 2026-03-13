package com.example.payrollpublisher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXG_NAME_PAYROLL_GENERATION = "payroll.generation.direct";
    public static final String QUEUE_NAME_GENERATION_PAYROLL = "payroll.generation.queue.";
    public static final String ROUTING_KEY_PAY_GEN = "payroll.generation.key";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME_GENERATION_PAYROLL, false, false, false);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXG_NAME_PAYROLL_GENERATION, false, false);
    }

    @Bean
    public Binding bindingBuilder() {
        return BindingBuilder.bind(queue())
                .to(directExchange())
                .with(ROUTING_KEY_PAY_GEN);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setExchange(EXG_NAME_PAYROLL_GENERATION);
        return rabbitTemplate;
    }
}
