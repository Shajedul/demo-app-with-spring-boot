//package com.example.battery_api.config;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.SimpleMessageConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Import(MessageBrokerConfig.class) // Import the configuration class
//class MessageBrokerConfigTest {
//
//    @Autowired
//    private Queue batteryQueue;
//
//    @Autowired
//    private SimpleMessageConverter converter;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Test
//    void testBatteryQueueConfiguration() {
//        // Assert that the queue is correctly configured
//        assertNotNull(batteryQueue, "The battery queue should not be null.");
//        assertEquals("batteryQueue", batteryQueue.getName(), "The queue name should be 'batteryQueue'.");
//        assertTrue(batteryQueue.isDurable(), "The battery queue should be durable.");
//    }
//
//    @Test
//    void testRabbitTemplateConfiguration() {
//        // Assert that the RabbitTemplate is correctly configured
//        assertNotNull(rabbitTemplate, "The RabbitTemplate should not be null.");
//        assertSame(converter, rabbitTemplate.getMessageConverter(),
//                "The RabbitTemplate should use the custom message converter.");
//    }
//
//
//
//    @AfterAll
//    static void tearDown() {
//        // Stop PostgreSQL Testcontainer
//        if (postgresContainer.isRunning()) {
//            postgresContainer.stop();
//        }
//    }
//}
