package com.example.battery_api.controller;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import com.example.battery_api.service.BatteryMapper;
import com.example.battery_api.service.BatteryService;
import jakarta.validation.*;
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
        System.out.println("HERE");
        List<Battery> validBatteries = new ArrayList<>();
        List<Map<String, Object>> invalidEntries = new ArrayList<>();

        for (int i = 0; i < requestDTOs.size(); i++) {
            BatteryRequestDTO requestDTO = requestDTOs.get(i);

            try {
                // Validate the DTO manually
                validateBatteryRequestDTO(requestDTO);

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

        // Save only the valid batteries
        List<Battery> savedBatteries = batteryService.saveBatteries(validBatteries);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("savedBatteries", batteryMapper.toDTOList(savedBatteries));
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

    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getBatteriesInRange(
            @RequestParam String startPostcode,
            @RequestParam String endPostcode) {
        List<Battery> batteries = batteryService.getBatteriesInRange(startPostcode, endPostcode);

        List<String> batteryNames = batteries.stream()
                .map(Battery::getName)
                .sorted()
                .toList();
        int totalCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).sum();
        double averageCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).average().orElse(0.0);

        Map<String, Object> response = Map.of(
                "batteries", batteryNames,
                "totalCapacity", totalCapacity,
                "averageCapacity", averageCapacity
        );

        return ResponseEntity.ok(response);
    }
}