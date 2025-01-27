import com.example.battery_api.BatteryApiApplication;
import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BatteryApiApplication.class)
@Testcontainers
public class BatteryRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.0")
            .withDatabaseName("battery_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @BeforeAll
    static void setup() {
        try {
            // Wait for the consumer to process the message
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Thread was interrupted during sleep", e);
        }
        // Start PostgreSQL container
        postgresContainer.start();

        // Set system properties for Spring Boot to use the Testcontainers PostgreSQL
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @Autowired
    private BatteryRepository batteryRepository;


    private static Battery createBattery(String name, String postcode, int wattCapacity) {
        Battery battery = new Battery();
        battery.setName(name);
        battery.setPostcode(postcode);
        battery.setWattCapacity(wattCapacity);
        return battery;
    }

    @BeforeEach
    void deleteAndSeedData(){
        batteryRepository.deleteAll();
        List<Battery> batteries = new ArrayList<>();

        // Add batteries to the list
        batteries.add(createBattery("Battery1", "1235", 300));
        batteries.add(createBattery("Battery2", "1210", 600));

        batteryRepository.saveAll(batteries);

    }

    @Test
    public void testFindByPostcodeBetween() {



        // Act: Fetch batteries by postcode range
        List<Battery> result = batteryRepository.findByPostcodeBetween("1200", "1240");
        System.out.println(result);

        // Assert: Validate the results
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("Battery1", "Battery2");
    }

    @Test
    public void testFindByPostcodeBetweenAndWattCapacityBetween() {

        // Act: Fetch batteries by postcode and watt capacity range
        List<Battery> result = batteryRepository.findByPostcodeBetweenAndWattCapacityBetween("1200", "1240", 400, 601);

        // Assert: Validate the results
        assertThat(result).hasSize(1);
        assertThat(result).extracting("name").containsExactlyInAnyOrder( "Battery2");
    }
}
