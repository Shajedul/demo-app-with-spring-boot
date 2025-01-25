package com.example.battery_api.service;

import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatteryService {
    private final BatteryRepository batteryRepository;

    public BatteryService(BatteryRepository batteryRepository) {
        this.batteryRepository = batteryRepository;
    }

    public List<Battery> saveBatteries(List<Battery> batteries) {
        return batteryRepository.saveAll(batteries);
    }

    public List<Battery> getBatteriesInRange(String startPostcode, String endPostcode) {
        return batteryRepository.findByPostcodeBetween(startPostcode, endPostcode);
    }
}
