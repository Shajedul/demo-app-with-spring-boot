package com.example.battery_api.controller;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import com.example.battery_api.service.BatteryMapper;
import com.example.battery_api.service.BatteryService;
import jakarta.validation.*;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing battery resources.
 * Provides endpoints for saving batteries and retrieving batteries within specified ranges.
 */
@RestController
@RequestMapping("/api/batteries")
@Validated
public class BatteryController {
    private static final Logger logger = LoggerFactory.getLogger(BatteryController.class);
    private final BatteryService batteryService;
    private final BatteryMapper batteryMapper;

    public BatteryController(BatteryService batteryService, BatteryMapper batteryMapper) {
        this.batteryService = batteryService;
        this.batteryMapper = batteryMapper;
    }

    /**
     * Endpoint to save multiple batteries. Validates each battery and handles invalid entries separately.
     * 
     * Example Request:
     * [
     *   {"name": "PowerCell A1", "postcode": "9002", "wattCapacity": 100},
     *   {"name": "TurboCharge X", "postcode": "1200", "wattCapacity": 200},
     *   {"name": "VoltMaster Z", "postcode": "", "wattCapacity": 50},
     *   {"name": "", "wattCapacity": 300},
     *   {}
     * ]
     *
     * Example Response:
     * {
     *   "savedBatteries": [
     *     {
     *       "id": null,
     *       "name": "PowerCell A1",
     *       "postcode": "9002", 
     *       "wattCapacity": 100
     *     },
     *     {
     *       "id": null,
     *       "name": "TurboCharge X",
     *       "postcode": "1200",
     *       "wattCapacity": 200
     *     }
     *   ],
     *   "invalidEntries": [
     *     {
     *       "data": {"name": "VoltMaster Z", "postcode": "", "wattCapacity": 50},
     *       "index": 2,
     *       "errors": {
     *         "postcode": [
     *           "Postcode must be between 0200 and 9999 and consist of exactly 4 digits",
     *           "Postcode cannot be null or blank"
     *         ]
     *       }
     *     },
     *     {
     *       "data": {"name": "", "postcode": null, "wattCapacity": 300},
     *       "index": 3,
     *       "errors": {
     *         "postcode": ["Postcode cannot be null or blank"],
     *         "name": ["Name cannot be blank"]
     *       }
     *     },
     *     {
     *       "data": {"name": null, "postcode": null, "wattCapacity": null},
     *       "index": 4,
     *       "errors": {
     *         "wattCapacity": ["Watt capacity is required"],
     *         "postcode": ["Postcode cannot be null or blank"],
     *         "name": ["Name can not be null", "Name cannot be blank"]
     *       }
     *     }
     *   ],
     *   "savedBatteriesCount": 2
     * }
     *
     * @param requestDTOs List of battery requests to process
     * @return Response containing:
     *         - savedBatteries: List of successfully validated and saved batteries
     *         - invalidEntries: List of invalid entries with their validation errors
     *         - savedBatteriesCount: Number of successfully saved batteries
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveBatteries(@RequestBody List<BatteryRequestDTO> requestDTOs) {
        logger.info("Received request to save {} batteries", requestDTOs.size());
        List<Battery> validBatteries = new ArrayList<>();
        List<Map<String, Object>> invalidEntries = new ArrayList<>();

        // Process each battery request
        for (int i = 0; i < requestDTOs.size(); i++) {
            BatteryRequestDTO requestDTO = requestDTOs.get(i);
            logger.debug("Processing battery request at index {}: {}", i, requestDTO);

            try {
                // Validate the DTO manually
                validateBatteryRequestDTO(requestDTO);
                logger.debug("Battery request at index {} passed validation", i);

                // If valid, map to entity
                Battery battery = batteryMapper.toEntity(requestDTO);
                validBatteries.add(battery);
            } catch (ConstraintViolationException ex) {
                logger.warn("Validation failed for battery at index {}: {}", i, ex.getMessage());
                // Collect validation errors
                Map<String, Object> errorEntry = new HashMap<>();
                errorEntry.put("index", i);
                errorEntry.put("data", requestDTO);
                errorEntry.put("errors", extractValidationErrors(ex));
                invalidEntries.add(errorEntry);
            }
        }
        // Publish valid batteries to the message broker (RabbitMQ)
        logger.info("Publishing {} valid batteries to message broker", validBatteries.size());
        batteryService.publishValidBatteries(validBatteries);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("savedBatteriesCount", validBatteries.size());
        response.put("savedBatteries", batteryMapper.toDTOList(validBatteries));
        response.put("invalidEntries", invalidEntries);

        logger.info("Successfully processed battery save request. Valid: {}, Invalid: {}", 
                validBatteries.size(), invalidEntries.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a single BatteryRequestDTO using Jakarta validation
     * @param requestDTO The DTO to validate
     * @throws ConstraintViolationException if validation fails
     */
    private void validateBatteryRequestDTO(BatteryRequestDTO requestDTO) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BatteryRequestDTO>> violations = validator.validate(requestDTO);

        if (!violations.isEmpty()) {
            logger.debug("Validation violations found: {}", violations);
            throw new ConstraintViolationException(violations);
        }
    }

    /**
     * Extracts validation errors from a ConstraintViolationException
     * @param ex The exception containing validation errors
     * @return Map of field names to their validation error messages
     */
    private Map<String, List<String>> extractValidationErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(), // Group by field name
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList()) // Collect multiple error messages in a list
                ));
    }

    /**
     * Endpoint to retrieve batteries within specified postcode range and capacity constraints.
     * 
     * Example Response:
     * {
     *     "batteries": [
     *         "MegaVolt 2X",
     *         "MegaVolt 4X",
     *         "Powergen"
     *     ],
     *     "totalCapacity": 1000.0,
     *     "averageCapacity": 250.0,
     *     "totalBatteries": 4
     * }
     *
     * @param startPostcode Lower bound of postcode range (inclusive), must be between 0200 and 9999
     * @param endPostcode Upper bound of postcode range (inclusive), must be between 0200 and 9999
     * @param minCapacity Optional minimum watt capacity filter
     * @param maxCapacity Optional maximum watt capacity filter
     * @return Response containing:
     *         - batteries: Sorted list of battery names in the specified range
     *         - totalCapacity: Sum of watt capacities of all matching batteries
     *         - averageCapacity: Average watt capacity of matching batteries
     *         - totalBatteries: Count of matching batteries
     */
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

        logger.info("Received request to get batteries in range. Postcodes: {} to {}, Capacity range: {} to {}", 
                startPostcode, endPostcode, minCapacity, maxCapacity);

        // Fetch batteries matching criteria
        List<Battery> batteries = batteryService.getBatteriesInRangeWithCapacity(
                startPostcode, endPostcode, minCapacity, maxCapacity);

        // Calculate statistics
        List<String> batteryNames = batteries.stream()
                .map(Battery::getName)
                .sorted()
                .toList();
        int totalCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).sum();
        double averageCapacity = batteries.stream().mapToInt(Battery::getWattCapacity).average().orElse(0.0);
        int totalBatteries = batteries.size();

        logger.info("Found {} batteries in range with total capacity {} and average capacity {}", 
                totalBatteries, totalCapacity, averageCapacity);

        // Build response
        Map<String, Object> response = Map.of(
                "batteries", batteryNames,
                "totalCapacity", totalCapacity,
                "averageCapacity", averageCapacity,
                "totalBatteries", totalBatteries
        );

        return ResponseEntity.ok(response);
    }

}