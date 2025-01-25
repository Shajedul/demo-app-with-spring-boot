package com.example.battery_api.service;

import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatteryService {
    private final BatteryRepository batteryRepository;

    @Autowired
    private EntityManager entityManager; // Inject EntityManager here

    public BatteryService(BatteryRepository batteryRepository) {
        this.batteryRepository = batteryRepository;
    }

    @Transactional
    public List<Battery> saveBatteries(List<Battery> batteries) {
        List<Battery> savedBatteries = new ArrayList<>();

        for (int i = 0; i < batteries.size(); i++) {
            savedBatteries.add(batteryRepository.save(batteries.get(i)));

            if (i % 50 == 0) {  // For example, after every 50 records
                entityManager.flush();   // Flushes the session
                entityManager.clear();   // Clears the persistence context
            }
        }
        return savedBatteries;
    }
    public List<Battery> getBatteriesInRange(String startPostcode, String endPostcode) {
        return batteryRepository.findByPostcodeBetween(startPostcode, endPostcode);
    }
}
