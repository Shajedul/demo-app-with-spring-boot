package com.example.battery_api.controller;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import com.example.battery_api.service.BatteryMapper;
import com.example.battery_api.service.BatteryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<BatteryResponseDTO>> saveBatteries(@Valid @RequestBody List<@Valid BatteryRequestDTO> requestDTOs) {
        System.out.println("Incoming request payload: " + requestDTOs);
        List<Battery> batteries = requestDTOs.stream().map(batteryMapper::toEntity).toList();
        List<Battery> savedBatteries = batteryService.saveBatteries(batteries);
        System.out.println(batteryMapper.toDTOList(savedBatteries));
        return ResponseEntity.ok(batteryMapper.toDTOList(savedBatteries));
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