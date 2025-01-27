package com.example.battery_api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BatteryRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validBatteryRequestDTO_shouldPassValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", "1234", 500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void blankName_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("", "1234", 500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                v.getMessage().contains("cannot be blank"));
    }

    @Test
    void nullName_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO(null, "1234", 500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                v.getMessage().contains("can not be null"));
    }

    @Test
    void invalidPostcode_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", "12AB", 500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("postcode") &&
                v.getMessage().contains("Postcode must be between 0200 and 9999 and consist of exactly 4 digits"));
    }

    @Test
    void nullPostcode_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", null, 500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("postcode") &&
                v.getMessage().contains("cannot be null or blank"));
    }

    @Test
    void wattCapacityOutOfRange_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", "1234", 1500);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("wattCapacity") &&
                v.getMessage().contains("Maximum watt capacity can be at least 1000 KW"));
    }

    @Test
    void negativeWattCapacity_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", "1234", -50);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("wattCapacity") &&
                v.getMessage().contains("must be at least 1 KW"));
    }

    @Test
    void nullWattCapacity_shouldFailValidation() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("Battery1", "1234", null);

        // Act
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("wattCapacity") &&
                v.getMessage().contains("Watt capacity is required"));
    }



    @Test
    void testGetName() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("PowerCell A1", "1001", 100);

        // Act
        String name = dto.getName();

        // Assert
        assertEquals("PowerCell A1", name, "The name should match the input value.");
    }

    @Test
    void testGetPostcode() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("PowerCell A1", "1001", 100);

        // Act
        String postcode = dto.getPostcode();

        // Assert
        assertEquals("1001", postcode, "The postcode should match the input value.");
    }

    @Test
    void testGetWattCapacity() {
        // Arrange
        BatteryRequestDTO dto = new BatteryRequestDTO("PowerCell A1", "1001", 100);

        // Act
        Integer wattCapacity = dto.getWattCapacity();

        // Assert
        assertEquals(100, wattCapacity, "The watt capacity should match the input value.");
    }

}
