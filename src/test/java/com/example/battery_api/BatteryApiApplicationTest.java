package com.example.battery_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatteryApiApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies if the application context loads without issues
        assertThat(true).isTrue();
    }
}
