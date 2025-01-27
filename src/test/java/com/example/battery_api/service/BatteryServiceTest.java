package com.example.battery_api.service;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.model.Battery;
import com.example.battery_api.repository.BatteryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatteryServiceTest {

    @Mock
    private BatteryRepository batteryRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BatteryService batteryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    private static Battery createBattery(String name, String postcode, int wattCapacity) {
        Battery battery = new Battery();
        battery.setName(name);
        battery.setPostcode(postcode);
        battery.setWattCapacity(wattCapacity);
        return battery;
    }

    @Test
    void testSaveBatteries() {
        // Mock battery requests
        // Create a list to hold Battery objects
        List<Battery> batteries = new ArrayList<>();

        // Add batteries to the list
        batteries.add(createBattery("PowerCell A1", "1001", 100));
        batteries.add(createBattery("TurboCharge X", "200", 200));
        // Utility method to create a Battery object



        when(batteryRepository.save(any(Battery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Battery> savedBatteries = batteryService.saveBatteries(batteries);

        assertEquals(batteries.size(), savedBatteries.size());
        verify(batteryRepository, times(batteries.size())).save(any(Battery.class));
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }

    @Test
    void testGetBatteriesInRange() {
        // Arrange
        String startPostcode = "2000";
        String endPostcode = "3000";

        // Mock battery requests
        // Create a list to hold Battery objects
        List<Battery> mockBatteries = new ArrayList<>();

        // Add batteries to the list
        mockBatteries.add(createBattery("PowerCell A1", "1001", 100));
        mockBatteries.add(createBattery("TurboCharge X", "200", 200));
        // Utility method to create a Battery object

        when(batteryRepository.findByPostcodeBetween(startPostcode, endPostcode)).thenReturn(mockBatteries);

        // Act
        List<Battery> result = batteryService.getBatteriesInRange(startPostcode, endPostcode);
        // Verify the interaction with the repository
        verify(batteryRepository, times(1)).findByPostcodeBetween(startPostcode, endPostcode);
    }

    @Test
    void testSaveBatteriesWithEmptyList() {
        // Prepare an empty list of batteries
        List<Battery> batteries = new ArrayList<>();

        // Invoke the service
        List<Battery> savedBatteries = batteryService.saveBatteries(batteries);

        // Verify the results
        assertTrue(savedBatteries.isEmpty());
        verify(batteryRepository, never()).save(any(Battery.class));
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }

    @Test
    void testSaveBatteriesBelowFlushThreshold() {
        List<Battery> batteries = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            batteries.add(createBattery("Battery " + i, "100" + i, 100 + i));
        }

        when(batteryRepository.save(any(Battery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Battery> savedBatteries = batteryService.saveBatteries(batteries);

        assertEquals(batteries.size(), savedBatteries.size());
        verify(batteryRepository, times(batteries.size())).save(any(Battery.class));
        verify(entityManager, times(0)).flush(); // Called after 50 and 100 batteries
        verify(entityManager, times(0)).clear();
    }


    @Test
    void testSaveBatteriesExceedingFlushThreshold() {
        List<Battery> batteries = new ArrayList<>();
        for (int i = 0; i < 105; i++) { // 105 batteries (2 flush/clear calls)
            batteries.add(createBattery("Battery " + i, "100" + i, 100 + i));
        }

        when(batteryRepository.save(any(Battery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Battery> savedBatteries = batteryService.saveBatteries(batteries);

        assertEquals(batteries.size(), savedBatteries.size());
        verify(batteryRepository, times(batteries.size())).save(any(Battery.class));
        verify(entityManager, times(2)).flush(); // Called after 50 and 100 batteries
        verify(entityManager, times(2)).clear();
    }

    @Test
    void testSaveBatteriesWithRepositoryException() {
        List<Battery> batteries = new ArrayList<>();
        batteries.add(createBattery("Battery1", "1001", 100));
        batteries.add(createBattery("Battery2", "1002", 200));

        when(batteryRepository.save(any(Battery.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> batteryService.saveBatteries(batteries));
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }


    @Test
    void testSaveBatteriesWithNullElement() {
        List<Battery> batteries = new ArrayList<>();
        batteries.add(createBattery("Battery1", "1001", 100));
        batteries.add(null); // Null element
        batteries.add(createBattery("Battery3", "1003", 300));

        assertThrows(NullPointerException.class, () -> batteryService.saveBatteries(batteries));
        verify(batteryRepository, times(1)).save(any(Battery.class)); // Only the first battery is saved before exception
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }

    @Test
    void testSaveBatteries_NullList() {
        assertThrows(NullPointerException.class, () -> batteryService.saveBatteries(null));
    }


    @Test
    void testSaveBatteries_Exactly50Elements() {
        List<Battery> batteries = new ArrayList<>();
        for (int i = 0; i < 50; i++) { // 105 batteries (2 flush/clear calls)
            batteries.add(createBattery("Battery " + i, "100" + i, 100 + i));
        }


        when(batteryRepository.save(any(Battery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Battery> result = batteryService.saveBatteries(batteries);

        assertEquals(50, result.size());
        verify(batteryRepository, times(50)).save(any(Battery.class));
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }


    @Test
    void testPublishValidBatteries() {
        // Arrange: Create a list of batteries
        List<Battery> batteries = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // 105 batteries (2 flush/clear calls)
            batteries.add(createBattery("Battery " + i, "100" + i, 100 + i));
        }

        // Act: Call the method
        batteryService.publishValidBatteries(batteries);

        // Assert: Verify RabbitTemplate was called with the correct arguments
        verify(rabbitTemplate, times(1)).convertAndSend("batteryQueue", batteries);
    }

    @Test
    void testPublishValidBatteries_EmptyList() {
        // Arrange: Empty list
        List<Battery> validBatteries = List.of();

        // Act: Call the method
        batteryService.publishValidBatteries(validBatteries);

        // Assert: Verify RabbitTemplate was called with an empty list
        verify(rabbitTemplate, times(1)).convertAndSend("batteryQueue", validBatteries);
    }


    @Test
    void testPublishValidBatteries_NullList() {
        // Act & Assert: Expect a NullPointerException
        assertThrows(NullPointerException.class, () -> batteryService.publishValidBatteries(null));

    }


    @Test
    void testProcessBatteries_ValidList() {

        // Arrange: Create a mock BatteryService
        BatteryService batteryServiceSpy = spy(new BatteryService(mock(BatteryRepository.class), mock(RabbitTemplate.class), mock(EntityManager.class)));

        // Arrange: Create a list of batteries
        List<Battery> batteries = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // 105 batteries (2 flush/clear calls)
            batteries.add(createBattery("Battery " + i, "100" + i, 100 + i));
        }

        // Stub saveBatteries to avoid saving actual data
        doReturn(batteries).when(batteryServiceSpy).saveBatteries(batteries);

        // Act: Call processBatteries
        batteryServiceSpy.processBatteries(batteries);

        // Assert: Verify saveBatteries was called
        verify(batteryServiceSpy, times(1)).saveBatteries(batteries);
    }

    @Test
    void testProcessBatteries_NullBatteries_ThrowsException() {
        // Arrange: Create a mock BatteryService
        BatteryService batteryServiceSpy = spy(new BatteryService(
                mock(BatteryRepository.class),
                mock(RabbitTemplate.class),
                mock(EntityManager.class))
        );

        // Act & Assert: Calling processBatteries(null) should throw NullPointerException
        Assertions.assertThrows(NullPointerException.class, () -> batteryServiceSpy.processBatteries(null));
    }



    // Test methods go here
}
