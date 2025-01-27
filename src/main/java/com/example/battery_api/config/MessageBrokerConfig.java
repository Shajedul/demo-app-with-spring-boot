package com.example.battery_api.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AllowedListDeserializingMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MessageBrokerConfig {

    // Define a queue for valid batteries
    @Bean
    public Queue batteryQueue() {
        return new Queue("batteryQueue", true); // true makes the queue durable
    }

    // Create a MessageConverter bean that uses AllowedListDeserializingMessageConverter
    @Bean
    public SimpleMessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        // Define the allowed patterns for deserialization
        converter.setAllowedListPatterns(List.of(
                "java.util.ArrayList", // Allow ArrayList class
                "com.example.battery_api.*" // Allow all classes in your package
        ));
        return converter;
    }

    // Define RabbitTemplate with custom MessageConverter
    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
