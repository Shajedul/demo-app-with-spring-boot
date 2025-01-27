package com.example.battery_api.repository;

import com.example.battery_api.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatteryRepository extends JpaRepository<Battery, Long> {
    List<Battery> findByPostcodeBetween(String start, String end);

    List<Battery> findByPostcodeBetweenAndWattCapacityBetween(
            String startPostcode, String endPostcode, int minCapacity, int maxCapacity);
}