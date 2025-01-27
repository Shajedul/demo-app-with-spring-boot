package com.example.battery_api.service;

import com.example.battery_api.dto.BatteryRequestDTO;
import com.example.battery_api.dto.BatteryResponseDTO;
import com.example.battery_api.model.Battery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatteryMapperTest {

    private BatteryMapper batteryMapper;

    @BeforeEach
    void setUp() {
        batteryMapper = new BatteryMapper();
    }

    /**
     * Test the toEntity method using a mocked BatteryRequestDTO.
     */
    @Test
    void testToEntity_withMockedDto() {
        // Arrange
        BatteryRequestDTO mockDto = mock(BatteryRequestDTO.class);
        when(mockDto.getName()).thenReturn("Mocked Battery");
        when(mockDto.getPostcode()).thenReturn("99999");
        when(mockDto.getWattCapacity()).thenReturn(500);

        // Act
        Battery battery = batteryMapper.toEntity(mockDto);

        // Assert
        assertNotNull(battery, "Battery entity should not be null");
        assertEquals("Mocked Battery", battery.getName(), "Name should match mock value");
        assertEquals("99999", battery.getPostcode(), "Postcode should match mock value");
        assertEquals(500.0, battery.getWattCapacity(), 0.001, "Watt capacity should match mock value");

        // Verify that the mapper indeed called getters on the DTO
        verify(mockDto, times(1)).getName();
        verify(mockDto, times(1)).getPostcode();
        verify(mockDto, times(1)).getWattCapacity();
    }

    /**
     * Test the toDTO method using a mocked Battery entity.
     */
    @Test
    void testToDTO_withMockedEntity() {
        // Arrange
        Battery mockBattery = mock(Battery.class);
        when(mockBattery.getId()).thenReturn(1L);
        when(mockBattery.getName()).thenReturn("Mocked Battery");
        when(mockBattery.getPostcode()).thenReturn("88888");
        when(mockBattery.getWattCapacity()).thenReturn(250);

        // Act
        BatteryResponseDTO dto = batteryMapper.toDTO(mockBattery);

        // Assert
        assertNotNull(dto, "BatteryResponseDTO should not be null");
        assertEquals(1L, dto.getId(), "ID should match mock value");
        assertEquals("Mocked Battery", dto.getName(), "Name should match mock value");
        assertEquals("88888", dto.getPostcode(), "Postcode should match mock value");
        assertEquals(250.0, dto.getWattCapacity(), 0.001, "Watt capacity should match mock value");

        // Verify that the mapper indeed called getters on the Battery entity
        verify(mockBattery, times(1)).getId();
        verify(mockBattery, times(1)).getName();
        verify(mockBattery, times(1)).getPostcode();
        verify(mockBattery, times(1)).getWattCapacity();
    }

    /**
     * Test the toDTOList method using a list of mocked Battery entities.
     */
    @Test
    void testToDTOList_withMockedEntities() {
        // Arrange
        Battery mockBattery1 = mock(Battery.class);
        when(mockBattery1.getId()).thenReturn(1L);
        when(mockBattery1.getName()).thenReturn("Battery One");
        when(mockBattery1.getPostcode()).thenReturn("11111");
        when(mockBattery1.getWattCapacity()).thenReturn(100);

        Battery mockBattery2 = mock(Battery.class);
        when(mockBattery2.getId()).thenReturn(2L);
        when(mockBattery2.getName()).thenReturn("Battery Two");
        when(mockBattery2.getPostcode()).thenReturn("22222");
        when(mockBattery2.getWattCapacity()).thenReturn(200);

        List<Battery> mockBatteryList = Arrays.asList(mockBattery1, mockBattery2);

        // Act
        List<BatteryResponseDTO> dtoList = batteryMapper.toDTOList(mockBatteryList);

        // Assert
        assertNotNull(dtoList, "Returned DTO list should not be null");
        assertEquals(2, dtoList.size(), "DTO list should have 2 elements");

        // Check first Battery DTO
        BatteryResponseDTO dto1 = dtoList.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Battery One", dto1.getName());
        assertEquals("11111", dto1.getPostcode());
        assertEquals(100.0, dto1.getWattCapacity(), 0.001);

        // Check second Battery DTO
        BatteryResponseDTO dto2 = dtoList.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Battery Two", dto2.getName());
        assertEquals("22222", dto2.getPostcode());
        assertEquals(200.0, dto2.getWattCapacity(), 0.001);

        // Verify that the mapper called getters on each mocked Battery
        verify(mockBattery1, times(1)).getId();
        verify(mockBattery1, times(1)).getName();
        verify(mockBattery1, times(1)).getPostcode();
        verify(mockBattery1, times(1)).getWattCapacity();

        verify(mockBattery2, times(1)).getId();
        verify(mockBattery2, times(1)).getName();
        verify(mockBattery2, times(1)).getPostcode();
        verify(mockBattery2, times(1)).getWattCapacity();
    }
}
