package com.example.battery_api.service;

import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatteryService {
    private final BatteryRepository batteryRepository;

    private final RabbitTemplate rabbitTemplate; // Message broker dependency

    @Autowired
    private EntityManager entityManager; // Inject EntityManager here

    @Autowired
    public BatteryService(BatteryRepository batteryRepository, RabbitTemplate rabbitTemplate, EntityManager entityManager) {
        this.batteryRepository = batteryRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.entityManager = entityManager;

    }

    @Transactional
    public List<Battery> saveBatteries(List<Battery> batteries) {
        List<Battery> savedBatteries = new ArrayList<>();

        for (int i = 0; i < batteries.size(); i++) {
            if (batteries.get(i) == null) {  // Check for null
                throw new NullPointerException("Battery at index " + i + " is null.");
            }

            savedBatteries.add(batteryRepository.save(batteries.get(i)));

            if (i % 50 == 0 && i != 0) {  // Flush and clear after every 50 records, but not when i == 0
                entityManager.flush();   // Flushes the session
                entityManager.clear();   // Clears the persistence context
            }
        }
        return savedBatteries;
    }

    public List<Battery> getBatteriesInRange(String startPostcode, String endPostcode) {
        return batteryRepository.findByPostcodeBetween(startPostcode, endPostcode);
    }

    public List<Battery> getBatteriesInRangeWithCapacity(
            String startPostcode, String endPostcode, Integer minCapacity, Integer maxCapacity) {

        // If minCapacity or maxCapacity is null, set default bounds
        int effectiveMinCapacity = (minCapacity != null) ? minCapacity : Integer.MIN_VALUE;
        int effectiveMaxCapacity = (maxCapacity != null) ? maxCapacity : Integer.MAX_VALUE;

        // Fetch batteries from repository
        return batteryRepository.findByPostcodeBetweenAndWattCapacityBetween(
                startPostcode, endPostcode, effectiveMinCapacity, effectiveMaxCapacity);
    }

    // Method to publish valid batteries to the message broker
    public void publishValidBatteries(List<Battery> validBatteries) {

        // Fail fast if the input is null
        if (validBatteries == null) {
            throw new NullPointerException("The batteries list cannot be null.");
        }
        // Publish all valid batteries as a single message to the queue
        rabbitTemplate.convertAndSend("batteryQueue", validBatteries);
        System.out.println("Published " + validBatteries.size() + " valid batteries to the queue.");
    }

    /**
     * Consumer to process batteries from the message broker.
     * Listens to the queue and processes valid batteries in batches.
     */
    @RabbitListener(queues = "${rabbitmq.queue.batteries}") // Queue name from application properties
    @Transactional
    public void processBatteries(List<Battery> batteries) {
        // Check if the batteries list is null
        if (batteries == null) {
            throw new NullPointerException("The batteries list cannot be null.");
        }

        System.out.println("Processing " + batteries.size() + " batteries from the queue.");

        saveBatteries(batteries); // Process and save batteries in batch
        System.out.println("Batch processing complete.");
    }
}
