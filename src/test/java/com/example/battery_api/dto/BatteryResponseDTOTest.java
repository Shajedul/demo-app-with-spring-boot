package com.example.battery_api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatteryResponseDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetterAndSetter() {
        // Arrange
        BatteryResponseDTO dto = new BatteryResponseDTO();
        dto.setId(1L);
        dto.setName("Battery1");
        dto.setPostcode("1234");
        dto.setWattCapacity(500);

        // Act & Assert
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Battery1");
        assertThat(dto.getPostcode()).isEqualTo("1234");
        assertThat(dto.getWattCapacity()).isEqualTo(500);
    }

    @Test
    void testConstructor() {
        // Arrange
        BatteryResponseDTO dto = new BatteryResponseDTO("Battery1", "1234", 500);

        // Act & Assert
        assertThat(dto.getName()).isEqualTo("Battery1");
        assertThat(dto.getPostcode()).isEqualTo("1234");
        assertThat(dto.getWattCapacity()).isEqualTo(500);
    }

    @Test
    void testJsonSerialization() throws JsonProcessingException {
        // Arrange
        BatteryResponseDTO dto = new BatteryResponseDTO();
        dto.setId(1L);
        dto.setName("Battery1");
        dto.setPostcode("1234");
        dto.setWattCapacity(500);

        // Act
        String json = objectMapper.writeValueAsString(dto);

        // Assert
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Battery1\"");
        assertThat(json).contains("\"postcode\":\"1234\"");
        assertThat(json).contains("\"wattCapacity\":500");
    }

    @Test
    void testJsonDeserialization() throws JsonProcessingException {
        // Arrange
        String json = """
                {
                  "id": 1,
                  "name": "Battery1",
                  "postcode": "1234",
                  "wattCapacity": 500
                }
                """;

        // Act
        BatteryResponseDTO dto = objectMapper.readValue(json, BatteryResponseDTO.class);

        // Assert
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Battery1");
        assertThat(dto.getPostcode()).isEqualTo("1234");
        assertThat(dto.getWattCapacity()).isEqualTo(500);
    }
}
