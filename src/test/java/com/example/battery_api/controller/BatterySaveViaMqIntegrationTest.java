package com.example.battery_api;

import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Use a test profile with in-memory database
@Testcontainers
public class BatterySaveViaMqIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.9-management")
            .withExposedPorts(5672, 15672); // Ports for RabbitMQ

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BatteryRepository batteryRepository;

    @BeforeAll
    static void setup() {

        try {
            // Wait for the consumer to process the message
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Thread was interrupted during sleep", e);
        }

        // Set RabbitMQ properties
        System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port", rabbitMQContainer.getMappedPort(5672).toString());

        // Set PostgreSQL properties
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @AfterAll
    static void tearDown() {
        // Stop RabbitMQ Testcontainer
        if (rabbitMQContainer.isRunning()) {
            rabbitMQContainer.stop();
        }

        // Stop PostgreSQL Testcontainer
        if (postgresContainer.isRunning()) {
            postgresContainer.stop();
        }
    }


    private static Battery createBattery(String name, String postcode, int wattCapacity) {
        Battery battery = new Battery();
        battery.setName(name);
        battery.setPostcode(postcode);
        battery.setWattCapacity(wattCapacity);
        return battery;
    }

    @Test
    public void testBatteriesSavedInDatabase() throws InterruptedException {
        // Arrange: Create a list of batteries to send to RabbitMQ
        List<Battery> batteries = new ArrayList<>();
        batteries.add(createBattery("Battery1", "1234", 500));
        batteries.add(createBattery("Battery2", "5678", 700));



        // Act: Publish the event to the RabbitMQ queue
        rabbitTemplate.convertAndSend("batteryQueue", batteries);

        // Wait for the consumer to process the message
        Thread.sleep(5000); // Adjust based on the application's processing speed

        // Assert: Check that the batteries are saved in the database
        List<Battery> savedBatteries = batteryRepository.findAll();
        assertThat(savedBatteries).hasSize(2);

        // Verify the details of the saved batteries
        assertThat(savedBatteries)
                .extracting(Battery::getName, Battery::getPostcode, Battery::getWattCapacity)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("Battery1", "1234", 500),
                        Tuple.tuple("Battery2", "5678", 700)
                );
    }



}
