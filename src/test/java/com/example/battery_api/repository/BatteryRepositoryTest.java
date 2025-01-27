package com.example.battery_api.repository;

import com.example.battery_api.model.Battery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BatteryRepositoryTest {

    @Autowired
    private BatteryRepository batteryRepository;

    private static Battery createBattery(String name, String postcode, int wattCapacity) {
        Battery battery = new Battery();
        battery.setName(name);
        battery.setPostcode(postcode);
        battery.setWattCapacity(wattCapacity);
        return battery;
    }

    @Test
    void findByPostcodeBetween_shouldReturnBatteriesInRange() {
        // Arrange
        Battery battery1 = createBattery("Battery1", "1234", 500);
        Battery battery2 = createBattery("Battery2", "1250", 700);
        Battery battery3 = createBattery("Battery3", "1300", 400);

        batteryRepository.saveAll(List.of(battery1, battery2, battery3));

        // Act
        List<Battery> batteriesInRange = batteryRepository.findByPostcodeBetween("1200", "1299");

        // Assert
        assertThat(batteriesInRange).hasSize(2);
        assertThat(batteriesInRange).extracting(Battery::getName)
                .containsExactlyInAnyOrder("Battery1", "Battery2");
    }

    @Test
    void findByPostcodeBetween_noBatteriesInRange_shouldReturnEmptyList() {
        // Arrange
        Battery battery1 = createBattery("Battery1", "1000", 500);
        Battery battery2 = createBattery("Battery2", "1500", 700);

        batteryRepository.saveAll(List.of(battery1, battery2));

        // Act
        List<Battery> batteriesInRange = batteryRepository.findByPostcodeBetween("1200", "1299");

        // Assert
        assertThat(batteriesInRange).isEmpty();
    }
}
