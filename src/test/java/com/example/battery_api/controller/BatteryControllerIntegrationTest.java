package com.example.battery_api.controller;

import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BatteryControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private static ConfigurableApplicationContext context;

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.9-management")
            .withExposedPorts(5672, 15672); // Ports for RabbitMQ

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("battery_db")
            .withUsername("testuser")
            .withPassword("testpass");


    @BeforeAll
    static void setup() {
        try {
            // Wait for the consumer to process the message
            Thread.sleep(5000); // Adjust based on the application's processing speed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Thread was interrupted during sleep", e);
        }

        // Set RabbitMQ properties
        System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port", rabbitMQContainer.getMappedPort(5672).toString());

        // Start PostgreSQL container
        postgresContainer.start();

        // Set system properties for Spring Boot to use the Testcontainers PostgreSQL
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

    @BeforeEach
    void deleteAll(){
        batteryRepository.deleteAll();

    }


    private static Battery createBattery(String name, String postcode, int wattCapacity) {
        Battery battery = new Battery();
        battery.setName(name);
        battery.setPostcode(postcode);
        battery.setWattCapacity(wattCapacity);
        return battery;
    }
    @Test
    public void testSaveBatteries() {
        String url = "http://localhost:" + port + "/api/batteries";

        // Create sample request data
        List<Map<String, Object>> requestDTOs = List.of(
                Map.of("name", "Battery1", "postcode", "1234", "wattCapacity", 500),
                Map.of("name", "", "postcode", "67890", "wattCapacity", 200)
        );

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Send the request
        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(requestDTOs, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        System.out.println(response);

        // Assert response
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsKeys("savedBatteriesCount", "savedBatteries", "invalidEntries");

        // Check valid entries
        int savedBatteriesCount = (int) responseBody.get("savedBatteriesCount");
        List<Map<String, Object>> savedBatteries = (List<Map<String, Object>>) responseBody.get("savedBatteries");
        assertThat(savedBatteriesCount).isEqualTo(1);
        assertThat(savedBatteries).hasSize(1);

        // Check invalid entries
        List<Map<String, Object>> invalidEntries = (List<Map<String, Object>>) responseBody.get("invalidEntries");
        assertThat(invalidEntries).hasSize(1);
        assertThat(invalidEntries.get(0)).containsKeys("index", "data", "errors");
        assertThat((int) invalidEntries.get(0).get("index")).isEqualTo(1);

        // Assert invalid entries details
        Map<String, Object> firstInvalidEntry = invalidEntries.get(0); // Get the first invalid entry

        // Assert the index of the invalid entry
        assertThat(firstInvalidEntry.get("index")).isEqualTo(1); // Index should be 1

        // Assert the data of the invalid entry
        Map<String, Object> invalidData = (Map<String, Object>) firstInvalidEntry.get("data");
        assertThat(invalidData).containsEntry("name", ""); // Name should be blank
        assertThat(invalidData).containsEntry("postcode", "67890"); // Postcode should match the invalid value
        assertThat(invalidData).containsEntry("wattCapacity", 200); // Watt capacity should match

        // Assert the errors of the invalid entry
        Map<String, List<String>> errors = (Map<String, List<String>>) firstInvalidEntry.get("errors");

        // Assert specific error messages for "name"
        assertThat(errors).containsKey("name");
        assertThat(errors.get("name")).containsExactly("Name cannot be blank");

        // Assert specific error messages for "postcode"
        assertThat(errors).containsKey("postcode");
        assertThat(errors.get("postcode")).containsExactly("Postcode must be between 0200 and 9999 and consist of exactly 4 digits");


    }



    @Test
    public void testGetBatteriesInRangeWithCapacityWithoutData() {
        String url = "http://localhost:" + port + "/api/batteries/with-range";

        // Set query parameters
        String startPostcode = "1000";
        String endPostcode = "2000";
        Integer minCapacity = 300;
        Integer maxCapacity = 700;

        // Build URL with query parameters
        String fullUrl = url + "?startPostcode=" + startPostcode +
                "&endPostcode=" + endPostcode +
                "&minCapacity=" + minCapacity +
                "&maxCapacity=" + maxCapacity;

        // Send GET request
        ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);
        System.out.println(response);

        // Assert response
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsKeys("batteries", "totalCapacity", "averageCapacity", "totalBatteries");

        // Cast the "batteries" field to a List<String>
        @SuppressWarnings("unchecked")
        List<String> batteryNames = (List<String>) responseBody.get("batteries");

        // Assert the battery names
        assertThat(batteryNames)
                .isEmpty();

        // Check total capacity
        int totalCapacity = (int) responseBody.get("totalCapacity");
        assertThat(totalCapacity).isEqualTo(0);

        // Check average capacity
        double averageCapacity = (double) responseBody.get("averageCapacity");
        assertThat(averageCapacity).isEqualTo(0.0);

        // Check total batteries count
        int totalBatteries = (int) responseBody.get("totalBatteries");
        assertThat(totalBatteries).isEqualTo(0); // Replace with expected count based on your database data
    }

    @Test
    public void testGetBatteriesInRangeWithCapacityWithData() {
        // Add sample batteries
        batteryRepository.saveAll(List.of(
                createBattery("PowerCell A1", "9002", 200),
                createBattery("TurboCharge A", "9940", 250),
                createBattery("PowerCell B", "9100", 150),
                createBattery("TurboCharge C", "0400", 100),
                createBattery("TurboCharge D", "4000", 50)
        ));
        String url = "http://localhost:" + port + "/api/batteries/with-range";

        // Set query parameters
        String startPostcode = "9000";
        String endPostcode = "9940";
        Integer minCapacity = 150;
        Integer maxCapacity = 250;

        // Build URL with query parameters
        String fullUrl = url + "?startPostcode=" + startPostcode +
                "&endPostcode=" + endPostcode +
                "&minCapacity=" + minCapacity +
                "&maxCapacity=" + maxCapacity;

        // Send GET request
        ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);
        System.out.println(response);

        // Assert response
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsKeys("batteries", "totalCapacity", "averageCapacity", "totalBatteries");

        // Cast the "batteries" field to a List<String>
        @SuppressWarnings("unchecked")
        List<String> batteries = (List<String>) responseBody.get("batteries");

        // Assert the battery names
        assertThat(batteries)
                .isNotEmpty();

        // Check total capacity
        int totalCapacity = (int) responseBody.get("totalCapacity");
        assertThat(totalCapacity).isEqualTo(600); // Total capacity is 600 in the response

        // Check average capacity
        double averageCapacity = (double) responseBody.get("averageCapacity");
        assertThat(averageCapacity).isEqualTo(200.0); // Average capacity is 200.0 in the response

        // Check total batteries count
        int totalBatteries = (int) responseBody.get("totalBatteries");
        assertThat(totalBatteries).isEqualTo(3); // Total batteries count is 3 in the response

        // Check battery names
        @SuppressWarnings("unchecked")
        List<String> batteryNames = (List<String>) responseBody.get("batteries");
        assertThat(batteryNames)
                .isNotEmpty()
                .containsExactlyInAnyOrder("PowerCell A1", "PowerCell B", "TurboCharge A"); // Replace with actual expected names

    }


    @Test
    public void testGetBatteriesInRangeWithCapacityWithDataWithoutCapacityParam() {

        // Add sample batteries
        batteryRepository.saveAll(List.of(
                createBattery("PowerCell A1", "9002", 200),
                createBattery("TurboCharge A", "9940", 250),
                createBattery("TurboCharge C", "0400", 100),
                createBattery("TurboCharge D", "4000", 50)
        ));
        String url = "http://localhost:" + port + "/api/batteries/with-range";

        // Set query parameters
        String startPostcode = "9000";
        String endPostcode = "9940";

        // Build URL with query parameters
        String fullUrl = url + "?startPostcode=" + startPostcode +
                "&endPostcode=" + endPostcode;
        // Send GET request
        ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);
        System.out.println(response);

        // Assert response
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsKeys("batteries", "totalCapacity", "averageCapacity", "totalBatteries");

        // Cast the "batteries" field to a List<String>
        @SuppressWarnings("unchecked")
        List<String> batteries = (List<String>) responseBody.get("batteries");

        // Check total capacity
        int totalCapacity = (int) responseBody.get("totalCapacity");
        assertThat(totalCapacity).isEqualTo(450); // Total capacity is 450 in the response

        // Check average capacity
        double averageCapacity = (double) responseBody.get("averageCapacity");
        assertThat(averageCapacity).isEqualTo(225.0); // Average capacity is 225.0 in the response

        // Check total batteries count
        int totalBatteries = (int) responseBody.get("totalBatteries");
        assertThat(totalBatteries).isEqualTo(2); // Total batteries count is 2 in the response

        // Check battery names
        @SuppressWarnings("unchecked")
        List<String> batteryNames = (List<String>) responseBody.get("batteries");
        assertThat(batteryNames)
                .isNotEmpty()
                .containsExactlyInAnyOrder("PowerCell A1", "TurboCharge A"); // Replace with actual expected names

    }

    @Test
    public void testGetBatteriesInRangeWithValidationError() {

        String url = "http://localhost:" + port + "/api/batteries/with-range";

        // Set invalid query parameters
        String startPostcode = "abcd";
        String endPostcode = "200";

        // Build URL with invalid query parameters
        String fullUrl = url + "?startPostcode=" + startPostcode +
                "&endPostcode=" + endPostcode;

        // Send GET request
        ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);
        System.out.println(response);

        // Assert response for validation error
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().toString()).contains("Start postcode must be between 0200 and 9999 and consist of exactly 4 digits");
        assertThat(response.getBody().toString()).contains("End postcode must be between 0200 and 9999 and consist of exactly 4 digits");
    }

}