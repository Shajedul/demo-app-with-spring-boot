package com.example.battery_api.controller;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import com.example.battery_api.service.BatteryMapper;
import com.example.battery_api.service.BatteryService;
import jakarta.validation.*;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batteries")
@Validated
public class BatteryController {
    private final BatteryService batteryService;
    private final BatteryMapper batteryMapper;

    public BatteryController(BatteryService batteryService, BatteryMapper batteryMapper) {
        this.batteryService = batteryService;
        this.batteryMapper = batteryMapper;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveBatteries(@RequestBody List<BatteryRequestDTO> requestDTOs) {
        List<Battery> validBatteries = new ArrayList<>();
        List<Map<String, Object>> invalidEntries = new ArrayList<>();

        for (int i = 0; i < requestDTOs.size(); i++) {
            BatteryRequestDTO requestDTO = requestDTOs.get(i);

            try {
                // Validate the DTO manually
                validateBatteryRequestDTO(requestDTO);
                System.out.println(requestDTO);

                // If valid, map to entity
                Battery battery = batteryMapper.toEntity(requestDTO);
                validBatteries.add(battery);
            } catch (ConstraintViolationException ex) {
                // Collect validation errors
                Map<String, Object> errorEntry = new HashMap<>();
                errorEntry.put("index", i);
                errorEntry.put("data", requestDTO);
                errorEntry.put("errors", extractValidationErrors(ex));
                invalidEntries.add(errorEntry);
            }
        }
        // Publish valid batteries to the message broker (RabbitMQ)
        batteryService.publishValidBatteries(validBatteries);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("savedBatteriesCount", validBatteries.size());
        response.put("savedBatteries", batteryMapper.toDTOList(validBatteries)); // Add all valid batteries to the response
        response.put("invalidEntries", invalidEntries);

        return ResponseEntity.ok(response);
    }

    private void validateBatteryRequestDTO(BatteryRequestDTO requestDTO) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(requestDTO);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private Map<String, List<String>> extractValidationErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(), // Group by field name
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList()) // Collect multiple error messages in a list
                ));
    }

    @GetMapping("/with-range")
    public ResponseEntity<Map<String, Object>> getBatteriesInRange(
            @RequestParam
            @Pattern(regexp = "^(0[2-9][0-9]{2}|[1-9][0-9]{3})$", message = "Start postcode must be between 0200 and 9999 and consist of exactly 4 digits")
            String startPostcode,
            @RequestParam
            @Pattern(regexp = "^(0[2-9][0-9]{2}|[1-9][0-9]{3})$", message = "End postcode must be between 0200 and 9999 and consist of exactly 4 digits")
            String endPostcode,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity) {

        List<Battery> batteries = batteryService.getBatteriesInRangeWithCapacity(
                startPostcode, endPostcode, minCapacity, maxCapacity);

        List<String> batteryNames = batteries.stream()
                .map(Battery::getName)
                .sorted()
                .toList();
        int totalCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).sum();
        double averageCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).average().orElse(0.0);
        int totalBatteries = batteries.size(); // Get the total number of batteries

        Map<String, Object> response = Map.of(
                "batteries", batteryNames,
                "totalCapacity", totalCapacity,
                "averageCapacity", averageCapacity,
                "totalBatteries", totalBatteries
        );

        return ResponseEntity.ok(response);
    }

}